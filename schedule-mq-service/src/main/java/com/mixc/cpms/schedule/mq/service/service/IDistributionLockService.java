package com.mixc.cpms.schedule.mq.service.service;

/**
 * @author Joseph
 * @since 2023/2/7
 */
public interface IDistributionLockService {

    void lock(String name, String owner, Integer validSeconds);

    void unlock(String name, String owner);
}
