package io.github.organizationApp.expensesProcess;

import javax.persistence.Embeddable;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Embeddable
public class Audit {
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    @PrePersist
    void prePersist() {
        this.createdOn = LocalDateTime.now();
    }
    @PreUpdate
    void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }
}
