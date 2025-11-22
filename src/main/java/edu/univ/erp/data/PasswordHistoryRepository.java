package edu.univ.erp.data;

import java.util.List;

/**
 * Data access abstraction for password history entries.
 */
public interface PasswordHistoryRepository {

    void recordPassword(long userId, String passwordHash);

    List<String> findRecentHashes(long userId, int limit);
}

