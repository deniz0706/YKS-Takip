package com.deniz0706.ykstakip.util

import org.junit.Assert.*
import org.junit.Test

class AiVisionCapabilitiesTest {
    @Test fun allowsOnlyTheVerifiedVisionModel() {
        assertTrue(AiVisionCapabilities.supportsImages(AiVisionCapabilities.VERIFIED_MODEL))
        assertTrue(AiVisionCapabilities.supportsImages("  ${AiVisionCapabilities.VERIFIED_MODEL.uppercase()}  "))
        assertFalse(AiVisionCapabilities.supportsImages("meta/llama-3.1-8b-instruct"))
        assertFalse(AiVisionCapabilities.supportsImages(""))
    }
}
