package org.example.agenticai.web;

import org.example.agenticai.service.AgenticService;
import org.example.agenticai.util.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agentic/")
public class AgenticController {

    @Autowired
    AgenticService service;

    @GetMapping("/basic")
    public String basicAgent(@RequestParam String topic) {

        return service.basicAgent(topic);
    }

    @GetMapping("/sequential")
    public String sequentialAgent(@RequestParam String topic,
                                  @RequestParam(defaultValue = "General Audience") String audience) {
        return service.sequential(topic, audience);
    }

    @GetMapping("/loop")
    public String loopAgentApi(@RequestParam String story) {
        return service.loop(story);
    }

    @GetMapping("/parallel")
    public Review parallelAgentApi(@RequestParam String text) {
        return service.parallelAgents(text);
    }

    @GetMapping("/mapper")
    public Object parallelAgentMapper(@RequestParam List<String> topics) {
        return service.mapper(topics);
    }

    @GetMapping("/conditional")
    public String conditionalAgentApi(@RequestParam String message) {
        return service.conditionalAgent(message);
    }

    @GetMapping("/translate")
    public Object translateAgentApi(@RequestParam String topic, @RequestParam String language) {
        return service.translatorAgent(topic, language);
    }

    @GetMapping("/streaming")
    public String streamingAgentApi(@RequestParam String topic) {
        return service.streaming(topic);
    }

    @GetMapping("/error-recovery")
    public String errorRecoveryAgentApi(@RequestParam String topic) {
        // Not using the parameter topic intentionally to test error recovery.
        return service.errorRecovery();
    }

    @GetMapping("/observability")
    public Object observabilityAgentApi(@RequestParam String topic) {
        return service.observability(topic);
    }

    @GetMapping("/human")
    public String humanInTheLoopApi(@RequestParam String request, @RequestParam String humanDecision) {
        return service.humanInTheLoop(request, humanDecision);
    }


}
