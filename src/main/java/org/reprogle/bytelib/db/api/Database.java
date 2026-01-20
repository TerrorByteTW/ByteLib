package org.reprogle.bytelib.db.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Database extends AutoCloseable {
    // Synchronous with sane blocking defaults
    int execute(String sql, Param<?>... params);
    <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params);
    <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params);

    // Synchronous with configurable blocking defaults (I.e. reducing timeout on event thread)
    int executeBlocking(String sql, BlockingOptions options, Param<?>... params);
    <T> List<T> queryBlocking(String sql, RowMapper<T> mapper, BlockingOptions options, Param<?>... params);
    <T> T queryOneBlocking(String sql, RowMapper<T> mapper, BlockingOptions options, Param<?>... params);

    // Transactions
    <T> T transaction(TransactionCallback<T> work);

    // Async
    CompletableFuture<Integer> executeAsync(String sql, Param<?>... params);
    <T> CompletableFuture<List<T>> queryAsync(String sql, RowMapper<T> mapper, Param<?>... params);

    @Override
    void close();
}
