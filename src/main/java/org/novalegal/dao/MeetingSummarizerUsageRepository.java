package org.novalegal.dao;

import org.novalegal.models.MeetingSummarizerUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingSummarizerUsageRepository extends JpaRepository<MeetingSummarizerUsage, Long> {
    Optional<MeetingSummarizerUsage> findByUuid(String uuid);

    Optional<MeetingSummarizerUsage> findByIpAddress(String ipAddress);

    Optional<MeetingSummarizerUsage> findFirstByIpAddressAndUserAgentAndReferer(
            String ipAddress,
            String userAgent,
            String referer
    );
}