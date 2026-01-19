package org.ject.recreation.storage.db.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    @Query("""
        select r
        from ReportEntity r
        join fetch r.game g
        join fetch g.gameCreator gc
        left join fetch r.reporter rp
        order by r.reportedAt desc
    """)
    Page<ReportEntity> findAllForAdmin(Pageable pageable);
}
