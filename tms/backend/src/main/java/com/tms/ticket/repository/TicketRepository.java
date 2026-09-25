package com.tms.ticket.repository;

import com.tms.common.enums.TicketStatus;
import com.tms.ticket.model.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.status = :status
            AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            AND (:keyword = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Ticket> findByFilters(
            @Param("status") TicketStatus status,
            @Param("assigneeId") Long assigneeId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
