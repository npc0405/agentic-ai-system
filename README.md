# Agentic AI System

A Spring Boot demo project showcasing the **LangChain4j Agentic** module. Each REST endpoint demonstrates one agentic workflow pattern: single agent, sequential, loop, parallel, parallel mapper, conditional routing, optional/async agents, streaming, error recovery, observability, and human-in-the-loop.

By default it runs against a local **Ollama** model. **OpenAI** is supported by switching the bean configuration.

## Tech Stack

| Component | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.16 |
| LangChain4j | 1.18.1 |
| LangChain4j Agentic | 1.18.1-beta28 |
| LLM provider | Ollama (default, `qwen3:14b`) or OpenAI (`gpt-4o-mini`) |

## Project Structure

```
src/main/java/org/example/agenticai/
├── AgenticAiSystemApplication.java   # Spring Boot entry point
├── agents/Agents.java                # All agent interfaces (prompts + @Agent metadata)
├── config/ModelConfig.java           # ChatModel / StreamingChatModel beans
├── service/AgenticService.java       # Workflow wiring for every pattern
├── web/AgenticController.java        # REST endpoints
└── util/
    ├── Intent.java                   # enum: QUESTION, COMPLAINT, PRAISE
    └── Review.java                   # record: (double score, String feedback)
```

## Prerequisites

- JDK 17+
- Maven 3.9+
- [Ollama](https://ollama.com) running locally with the model pulled:

```bash
ollama pull qwen3:14b
ollama serve
```

## Configuration

`src/main/resources/application.properties`:

```properties
spring.application.name=agentic-ai-system

# OpenAI (optional)
openai.api.key=
openai.model=gpt-4o-mini

# Ollama (default)
ollama.base-url=http://localhost:11434
ollama.model-name=qwen3:14b
```

### Switching to OpenAI

In `ModelConfig.java`, comment out the two Ollama beans, uncomment the OpenAI ones, and set the key via an environment variable. Never commit it:

```bash
export OPENAI_API_KEY=sk-...
```

```properties
openai.api.key=${OPENAI_API_KEY}
```

## Running

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

## API Reference

Base path: `/api/agentic`. All endpoints are `GET`.

| Pattern | Endpoint | Params | Description |
|---|---|---|---|
| Basic agent | `/basic` | `topic` | Single `StoryWriter` agent writes a short story. |
| Sequential | `/sequential` | `topic`, `audience` (default `General Audience`) | Writer → AudienceEditor → StyleEditor pipeline. |
| Loop | `/loop` | `story` | Score → improve, up to 2 iterations, exits once score ≥ 0.8. |
| Parallel | `/parallel` | `text` | SEO and readability reviewers run concurrently, results merged into one `Review`. |
| Parallel mapper | `/mapper` | `topics` (repeatable) | Fans out one summarizer agent over a list of topics. |
| Conditional | `/conditional` | `message` | Classifies a customer message, then routes to the question, complaint, or praise responder. |
| Optional + async | `/translate` | `topic`, `language` | Writes a story, translates it (optional agent), and runs a fact-checker asynchronously. |
| Streaming | `/streaming` | `topic` | Streams tokens from the model and returns the assembled story. |
| Error recovery | `/error-recovery` | `topic` (ignored) | Invokes the writer without a topic and recovers by injecting a default and retrying. |
| Observability | `/observability` | `topic` | Runs an agent with an `AgentListener` that logs invocations and scope lifecycle. |
| Human-in-the-loop | `/human` | `request`, `humanDecision` | Agent proposes a decision, and the supplied `humanDecision` acts as approval. |

### Example calls

```bash
curl "http://localhost:8080/api/agentic/basic?topic=a%20lighthouse%20keeper"

curl "http://localhost:8080/api/agentic/sequential?topic=space%20travel&audience=children"

curl "http://localhost:8080/api/agentic/parallel?text=Your%20article%20text%20here"

curl "http://localhost:8080/api/agentic/mapper?topics=quantum%20computing&topics=blockchain&topics=RAG"

curl "http://localhost:8080/api/agentic/conditional?message=My%20order%20arrived%20damaged"

curl "http://localhost:8080/api/agentic/human?request=Refund%20order%20123&humanDecision=approved"
```

## Agents

| Agent | Role |
|---|---|
| `StoryWriter` | Writes a short story from a topic |
| `AudienceEditor` | Rewrites the story for a target audience |
| `StyleEditor` | Polishes style, keeps the plot |
| `StyleScorer` | Scores writing quality (0.0–1.0) with feedback |
| `StyleImprover` | Revises a story using reviewer feedback |
| `SeoReviewer` / `ReadabilityReview` | Score SEO friendliness / readability |
| `TopicSummarizer` | Two-sentence topic summary |
| `Classifier` | Returns an `Intent` (QUESTION / COMPLAINT / PRAISE) |
| `QuestionResponder` / `ComplaintResponder` / `PraiseResponder` | Intent-specific replies |
| `Translator` | Translates text to a given language |
| `FactChecker` | Flags one fact worth verifying |
| `DecisionProposer` | Proposes a decision for a human to confirm |

## Key Concepts Demonstrated

- **Shared state via `AgenticScope`**: agents write results under an `outputKey` and downstream agents read them as template variables (`{{story}}`).
- **Typed outputs**: agents return a `Review` record or an `Intent` enum directly.
- **Workflow builders**: `sequenceBuilder`, `loopBuilder`, `parallelBuilder`, `parallelMapperBuilder`, `conditionalBuilder`, `humanInTheLoopBuilder`.
- **Resilience**: `errorHandler` with `ErrorRecoveryResult.retry()`.
- **Observability**: `AgentListener` hooks for agent and scope lifecycle.

## Testing

```bash
mvn test
```

Currently there is only a Spring context-load test. Note that it requires the `openai.*` and `ollama.*` properties to resolve.

## Known Limitations

- Local 14B models can be slow, so the client timeout is set to 5 minutes.
- Output quality and structured-output reliability (`Review`, `Intent`) depend on the model used.
- This is a demo: there is no authentication, rate limiting, or persistence.
