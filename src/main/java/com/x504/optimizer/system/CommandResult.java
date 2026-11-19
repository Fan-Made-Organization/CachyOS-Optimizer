package com.x504.optimizer.system;

public record CommandResult(int exitCode, String standardOutput, String standardError,
                            boolean timedOut) {
    public boolean successful() { return exitCode == 0 && !timedOut; }
}
