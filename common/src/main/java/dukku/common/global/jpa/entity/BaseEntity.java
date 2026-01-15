package dukku.common.global.jpa.entity;

import dukku.common.global.config.GlobalConfig;
import dukku.common.standard.modelType.CanGetModelTypeCode;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
// 모든 엔티티들의 조상
public abstract class BaseEntity implements CanGetModelTypeCode {

    protected void publishEvent(Object event) {
        GlobalConfig.getEventPublisher().publish(event);
    }

    public abstract int getId();
    public abstract LocalDateTime getCreatedAt();
    public abstract LocalDateTime getUpdatedAt();

    @Override
    public String getModelTypeCode() {
        return this.getClass().getSimpleName();
    }
}