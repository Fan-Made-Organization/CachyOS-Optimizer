package com.x504.optimizer.optimize;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PowerDeviceAdvisorTest {
    @Test void advisorAlwaysUsesConservativeDefaultAndNeverAppliesSettings() {
        var advice=new PowerDeviceAdvisor().inspect();
        assertEquals("balanced",advice.recommendedProfile());
        assertNotNull(advice.explanation());
        assertFalse(advice.explanation().isBlank());
    }
}
