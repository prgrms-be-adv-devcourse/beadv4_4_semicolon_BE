package dukku.ai.app.usecase;

import dukku.ai.entity.AiMemory;
import dukku.ai.out.AiMemoryRepository;
import dukku.common.shared.ai.type.MemorySubType;
import dukku.common.shared.ai.type.MemoryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InitializeAiUserUseCase {

    private final AiMemoryRepository aiMemoryRepository;

    public InitializeAiUserUseCase(AiMemoryRepository aiMemoryRepository) {
        this.aiMemoryRepository = aiMemoryRepository;
    }

    @Transactional
    public void execute(UUID userUuid, String nickname) {
        if (aiMemoryRepository.existsByUserUuidAndMemoryType(userUuid, MemoryType.PROFILE)) {
            return;
        }

        AiMemory profileMemory = AiMemory.builder()
                .userUuid(userUuid)
                .memoryType(MemoryType.PROFILE)
                .subType(MemorySubType.GENERAL)
                .content("Profile initialized for user: " + nickname)
                .importanceScore(1.0)
                .confidenceScore(1.0)
                .build();

        aiMemoryRepository.save(profileMemory);
    }
}
