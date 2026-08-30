package org.example.agenticai.service;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.example.agenticai.agents.Agents.*;
import org.example.agenticai.util.Intent;
import org.example.agenticai.util.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AgenticService {
    @Autowired
    private ChatModel model;

    @Autowired
    private StreamingChatModel streamingChatModel;

    public String basicAgent(String topic){
        StoryWriter writer = AgenticServices.agentBuilder(StoryWriter.class)
                .chatModel(model)
                .outputKey("story")
                .build();
        return writer.write(topic);
    }

    public String sequential(String topic, String audience) {

        // Firrst agent writes the story
        StoryWriter writer = AgenticServices.agentBuilder(StoryWriter.class)
                .chatModel(model)
                .outputKey("story")
                .build();

        // Second agent = modifies story as per target audience
        AudienceEditor audienceEditor = AgenticServices.agentBuilder(AudienceEditor.class)
                .chatModel(model)
                .outputKey("story")
                .build();

        // Third agent = Polishes the writing style, keeping the plot as is.
        StyleEditor styleEditor = AgenticServices.agentBuilder(StyleEditor.class)
                .chatModel(model)
                .outputKey("story")
                .build();

        // orchestrating them in sequential manner.
        UntypedAgent pipeline = AgenticServices.sequenceBuilder()
                .subAgents(writer, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        return pipeline.invoke(Map.of("topic", topic, "audience", audience)).toString();
    }

    public String loop(String story) {
//        // First agent writes the story
//        StoryWriter writer = AgenticServices.agentBuilder(StoryWriter.class)
//                .chatModel(model)
//                .outputKey("story")
//                .build();

        // Second agent scores the given story.
        StyleScorer scorer = AgenticServices.agentBuilder(StyleScorer.class)
                .chatModel(model)
                .outputKey("review")        // Returns review object and store it under "review".
                .build();

        Review res = scorer.score(story);
        System.out.println("Score :"+res.score());
        System.out.println("Feedback : "+ res.feedback());

        StyleImprover styleImprover = AgenticServices.agentBuilder(StyleImprover.class)
                .chatModel(model)
                .outputKey("story")
                .build();

        UntypedAgent refiner = AgenticServices.loopBuilder()
                .subAgents(/*writer,*/ scorer, styleImprover)
                .outputKey("story")
                .maxIterations(2)
                .exitCondition(scope -> ((Review)scope.readState("review")).score() >= 0.8)
                .build();
        return (String) refiner.invoke(
                Map.of("story", story));
    }

    public Review parallelAgents(String text) {

        SeoReviewer seo = AgenticServices.agentBuilder(SeoReviewer.class)
                .chatModel(model)
                .outputKey("seoReview")
                .build();
        ReadabilityReview readability = AgenticServices.agentBuilder(ReadabilityReview.class)
                .chatModel(model)
                .outputKey("readabilityReview")
                .build();

        var executor = Executors.newFixedThreadPool(2);

        UntypedAgent pipeline = AgenticServices.parallelBuilder()
                .subAgents(seo, readability)
                .executor(executor)
                .outputKey("finalReview")
                .output(agenticScope -> {
                    Review a = (Review) agenticScope.readState("seoReview");

                    Review b = (Review) agenticScope.readState("readabilityReview");

                    // Note: due to operator precedence, this currently calculates
                    // SEO score + (readability score / 2), not a simple average.
                    return new Review(
                            a.score() + b.score() / 2.0,
                            "SEO: " + a.feedback() + " | Readability: " + b.feedback()
                    );
                })
                .build();
        Review result = (Review) pipeline.invoke(Map.of("story", text));
        executor.shutdown();
        return result;
    }

    public Object mapper(List<String> topics) {
        TopicSummarizer summariser = AgenticServices.agentBuilder(TopicSummarizer.class)
                .chatModel(model)
                .outputKey("summary")
                .build();

        var executor = Executors.newFixedThreadPool(4);

        UntypedAgent batch = AgenticServices.parallelMapperBuilder()
                .subAgents(summariser)
                .itemsProvider("topics")
                .outputKey("summaries")
                .executor(executor)
                .build();

        Object result = batch.invoke(Map.of("topics", topics));
        executor.shutdown();
        return result;

    }

    public String conditionalAgent(String message) {
        // First create the classifier agent.
        Classifier classifier =
                AgenticServices.agentBuilder(Classifier.class)
                        .chatModel(model)

                        // Classifier returns an Intent.
                        .outputKey("intent")
                        .build();
        String res=classifier.classify(message).toString();
        System.out.println("Res: "+res);


        // Agent used when intent is QUESTION.
        QuestionResponder question =
                AgenticServices.agentBuilder(QuestionResponder.class)
                        .chatModel(model)
                        .outputKey("answer")
                        .build();

        String reply=question.reply(message);
        System.out.println("Reply: "+reply);


        // Agent used when intent is COMPLAINT.
        ComplaintResponder complaint =
                AgenticServices.agentBuilder(ComplaintResponder.class)
                        .chatModel(model)
                        .outputKey("answer")
                        .build();


        // Agent used when intent is PRAISE.
        PraiseResponder praise =
                AgenticServices.agentBuilder(PraiseResponder.class)
                        .chatModel(model)
                        .outputKey("answer")
                        .build();
        // Exactly one responder is chosen by comparing the classifier's "intent" state.
        UntypedAgent pipeline =
                AgenticServices.conditionalBuilder()
                        .subAgents(
                                agenticScope -> agenticScope.readState("intent") == Intent.QUESTION, question
                        )
                        .subAgents(
                                agenticScope -> agenticScope.readState("intent") == Intent.COMPLAINT, complaint

                        ).subAgents(
                                agenticScope -> agenticScope.readState("intent") == Intent.PRAISE, praise

                        )
                        .build();
        // The classifier must run first so the conditional router can read "intent".
        UntypedAgent finalPipeline = AgenticServices.sequenceBuilder()
                .subAgents(classifier, pipeline)
                .outputKey("answer")

                .build();
        return (String) finalPipeline.invoke(Map.of(
                        "message", message
                )
        );
    }
}
