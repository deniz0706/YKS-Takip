package com.deniz0706.ykstakip.util

import org.junit.Assert.*
import org.junit.Test

class PlanActionParserTest {
    private fun response(json: String) =
        "Bu haftalık plana eklenebilir.\n[[YKS_PLAN_ACTION]]$json[[/YKS_PLAN_ACTION]]"

    @Test fun parsesValidProposal() {
        val draft = PlanActionParser.parse(response(
            """{"type":"add_plan","dayOfWeek":1,"startTime":"09:30","endTime":"11:00","subject":"Matematik","topic":"Fonksiyonlar"}"""
        ))
        assertNotNull(draft)
        assertEquals(1, draft!!.dayOfWeek)
        assertEquals("09:30", draft.startTime)
        assertEquals("Fonksiyonlar", draft.topic)
    }

    @Test fun rejectsInvalidDayAndTimeRange() {
        assertNull(PlanActionParser.parse(response(
            """{"type":"add_plan","dayOfWeek":8,"startTime":"09:00","endTime":"10:00","subject":"Matematik","topic":""}"""
        )))
        assertNull(PlanActionParser.parse(response(
            """{"type":"add_plan","dayOfWeek":1,"startTime":"11:00","endTime":"10:00","subject":"Matematik","topic":""}"""
        )))
    }

    @Test fun rejectsUnknownActionsAndMalformedPayloads() {
        assertNull(PlanActionParser.parse(response(
            """{"type":"delete_all","dayOfWeek":1,"startTime":"09:00","endTime":"10:00","subject":"Matematik","topic":""}"""
        )))
        assertNull(PlanActionParser.parse(response("""{"type":"add_plan"}""")))
        assertNull(PlanActionParser.parse("No action here"))
    }

    @Test fun removesActionMetadataFromVisibleText() {
        val raw = response(
            """{"type":"add_plan","dayOfWeek":1,"startTime":"09:00","endTime":"10:00","subject":"Matematik","topic":""}"""
        )
        assertEquals("Bu haftalık plana eklenebilir.", PlanActionParser.visibleText(raw))
    }
}
