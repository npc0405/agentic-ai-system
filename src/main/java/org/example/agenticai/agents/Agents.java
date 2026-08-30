package org.example.agenticai.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.example.agenticai.util.Intent;
import org.example.agenticai.util.Review;
import org.springframework.context.annotation.Bean;

public class Agents {
    private Agents() {}

    public interface StoryWriter{
        @UserMessage("Write a short, vivid story  about {{topic}}")
        @Agent("Write a short story from the topic.")
        String write(@V("topic") String topic);
    }

    public interface AudienceEditor{
        @UserMessage("Rewrite the {{story}} appropriately for the given {{audience}}")
        @Agent("Rewrites the story for target audience. ")
        String edit(@V("story") String story, @V("audience") String audience);
    }

    public interface StyleEditor {
        @UserMessage("Polishes the style of the story, keeping the plot intact. Return back story only: {{story}}")
        @Agent("Polishes the writing style of the story.")
        String polish(@V("story") String story);
    }

    public interface StyleScorer {
        @UserMessage(
                """
        Evaluate the writing quality of the following story.

        Give a score from 0.0 to 1.0 using this rubric:

        0.0 - 0.2 : Very poor writing. Major problems with grammar,
                    clarity, structure, or readability.

        0.2 - 0.4 : Basic writing. Understandable but has several
                    weaknesses in grammar, clarity, detail, or flow.

        0.4 - 0.6 : Average writing. Mostly clear with some
                    noticeable issues.

        0.6 - 0.8 : Good writing. Clear, readable, well-structured,
                    with only minor issues.

        0.8 - 1.0 : Excellent writing. Clear, engaging, polished,
                    well-structured, and easy to read.

        IMPORTANT:
        - Be reasonably generous when the story is understandable.
        - Give scores based on the actual quality of the story.
        - Do not automatically give a very low score.
        - Return the score between 0.0 and 1.0.
        - Provide one short line of feedback.

        Story:
        {{story}}
        """
        )
        @Agent("Reviews and scores the story and suggests improvements.")
        Review score(@V("story") String story);
    }

    /** Revises a story by using the feedback produced by a reviewer. */
    public interface StyleImprover {
        @UserMessage("Improve the story using this feedback: {{review}}. Return only the story. Story: {{story}}")
        @Agent("Improves the story based on feedback")
        String improve(@V("story") String story, @V("review") Review review);
    }

    public interface SeoReviewer {
        @UserMessage("Rate the SEO friendliness for this text scoring from 0.0 to 1.0" +
                " with one improvement tip. Text: {{story}}")
        @Agent("Reviews the story for search friendliness.")
        Review seoReview(@V("story") String story);
    }

    public interface ReadabilityReview {
        @UserMessage("Rate how easy this text is to read from 0.0 to 1.0 with one improvement tip. " +
                "Text: {{story}}")
        @Agent("Reviews the text for readability")
        Review readability(@V("story") String story);
    }

    public interface TopicSummarizer {

        @UserMessage("Summarise this topic exactly in 2 sentences. Topic: {{topic}}")
        @Agent("Summarises a single topic")
        String summarise(@V("topic") String topic);
    }

    /** Routes a customer message to a response agent by returning an {@link Intent}. */
    public interface Classifier {
        // A system message sets the agent's enduring role/instruction.
        @SystemMessage("You classify customer messages")
        @UserMessage("Classify this message as QUESTION, COMPLAINT or PRAISE: {{message}}")
        @Agent("Classifies the customer's message")
        Intent classify(@V("message") String message);
    }

    /** Produces a concise, helpful answer for messages classified as questions. */
    public interface QuestionResponder {
        @UserMessage("Answer this customer question helpfully in two sentences: {{message}}")
        @Agent("Answers questions")
        String reply(@V("message") String message);
    }

    /** Produces an empathetic response and a practical next step for complaints. */
    public interface ComplaintResponder {
        @UserMessage("Respond to this complaint with empathy and a next step, in two sentences: {{message}}")
        @Agent("Handles complaints")
        String reply(@V("message") String message);
    }

    /** Produces a short thank-you response for positive customer messages. */
    public interface PraiseResponder {
        @UserMessage("Thank the customer warmly for this positive message in one sentence: {{message}}")
        @Agent("Handles praise")
        String reply(@V("message") String message);
    }
}
