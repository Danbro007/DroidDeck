package com.droiddeck.launcher.gpu

import org.junit.Assert.assertEquals
import org.junit.Test

class GpuModelFallbackTest {
    @Test fun aPlatformCodeNameGivesItsAdreno() {
        assertEquals(750, GpuInfo.platformModel("pineapple"))
        assertEquals(740, GpuInfo.platformModel("KALAMA"))
        assertEquals(830, GpuInfo.platformModel("sun"))
        assertEquals(0, GpuInfo.platformModel("somethingelse"))
        assertEquals(GpuInfo.Family.A7XX, GpuInfo.familyOf(true, GpuInfo.platformModel("pineapple")))
    }
    @Test fun sm8975IsTheOnlyNewSocFallback() {
        assertEquals(850, GpuInfo.platformModel("unknown", "", "SM8975"))
        assertEquals(850, GpuInfo.platformModel("unknown", "", "QTI SM8975"))
        assertEquals(850, GpuInfo.platformModel("unknown", "QTI SM8975", ""))
        assertEquals(850, GpuInfo.platformModel("unknown", "", " qti sm8975 "))
        assertEquals(0, GpuInfo.platformModel("unknown", "", "SM89750"))
        assertEquals(0, GpuInfo.platformModel("unknown", "", "SM8975-PRO"))
        assertEquals(0, GpuInfo.platformModel("unknown", "", "Qualcomm SM8975"))
        assertEquals(0, GpuInfo.platformModel("unknown", "", "SM8850"))
        assertEquals(850, GpuInfo.platformModel("sun", "", "SM8975"))
        assertEquals(830, GpuInfo.platformModel("sun", "", "unknown"))
    }

    @Test fun aKernelModelDoesNotNeedVulkanOrSocFallback() {
        assertEquals(850 to "kernel", GpuInfo.modelFromNames("Adreno850v2",
            { error("Vulkan must not be queried when the kernel identifies the GPU") },
            { error("SoC must not override the kernel model") }))
    }

    @Test fun aMissingKernelModelUsesVulkanBeforeSocFallback() {
        assertEquals(830 to "vulkan", GpuInfo.modelFromNames("Adreno", { "Adreno (TM) 830" },
            { error("SoC must not override the Vulkan model") }))
        assertEquals(850 to "vulkan", GpuInfo.modelFromNames(null, { "Adreno850v2" }, { 0 }))
    }

    @Test fun numericChipIdsCannotHideTheSocFallback() {
        for (name in listOf(null, "", "Adreno", "44051431", "0x2a02be7")) {
            assertEquals(850 to "platform", GpuInfo.modelFromNames(name, { "Adreno" },
                { GpuInfo.platformModel("unknown", "", "QTI SM8975") }))
        }
        assertEquals(0 to "", GpuInfo.modelFromNames(null, { null }, { 0 }))
    }
}
