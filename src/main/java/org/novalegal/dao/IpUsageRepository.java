package org.novalegal.dao;

import org.novalegal.models.IpUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IpUsageRepository extends JpaRepository<IpUsage, Long> {
    Optional<IpUsage> findByIpAddress(String ipAddress);
}
