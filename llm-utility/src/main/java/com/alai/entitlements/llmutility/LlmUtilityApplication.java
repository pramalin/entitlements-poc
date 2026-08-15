package com.alai.entitlements.llmutility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class LlmUtilityApplication {

    public static void main(String[] args) {
        // Deliberately NOT `SpringApplication.run(...)` and stopping there: this is a
        // one-off batch job, not a service. Explicitly exiting the JVM after the
        // CommandLineRunner finishes is what makes `docker compose run llm-utility`
        // return control to the shell instead of hanging like a server would.
        ConfigurableApplicationContext context = SpringApplication.run(LlmUtilityApplication.class, args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }

    @Bean
    CommandLineRunner run(EntitlementRepository repository,
                           DescriptionGenerator generator,
                           @Value("${spring.ai.openai.chat.model}") String modelName) {
        return args -> {
            List<EntitlementRow> pending = repository.findPending();

            if (pending.isEmpty()) {
                System.out.println("Nothing to do - every entitlement already has a description.");
                return;
            }

            System.out.printf("Generating descriptions for %d entitlement(s) using %s...%n",
                    pending.size(), modelName);

            int succeeded = 0;
            int failed = 0;

            for (EntitlementRow row : pending) {
                try {
                    DescriptionResult result = generator.generate(row);
                    repository.save(row.entitlementId(), result, modelName);
                    succeeded++;
                    System.out.printf("  [ok]     %-30s %s%n", row.crypticTitle(), truncate(result.description()));
                    if (result.riskNote() != null) {
                        System.out.printf("           ⚠ %s%n", result.riskNote());
                    }
                } catch (Exception e) {
                    failed++;
                    System.err.printf("  [FAILED] %-30s %s%n", row.crypticTitle(), e.getMessage());
                }
            }

            System.out.printf("%nDone. %d succeeded, %d failed out of %d.%n", succeeded, failed, pending.size());
            if (failed > 0) {
                System.out.println("Re-run the utility to retry the failed ones - it only processes entitlements still missing a description.");
            }
        };
    }

    private static String truncate(String s) {
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }
}
