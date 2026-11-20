package dev.hamper.tabula.system

import dev.hamper.tabula.compat.AudioManager.AUDIOFOCUS_GAIN
import dev.hamper.tabula.compat.AudioManager.AUDIOFOCUS_LOSS
import dev.hamper.tabula.compat.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import dev.hamper.tabula.compat.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipFocusTest {
    /** A player and an audio system in one: pause/resume feed back into the machine the way the player listener does. */
    private class Fake(var grant: Boolean = true) : ClipFocus.Port {
        val log = mutableListOf<String>()
        lateinit var f: ClipFocus
        override fun request() = grant.also { log += "request" }
        override fun abandon() { log += "abandon" }
        override fun pause() { log += "pause"; f.playing(false) }
        override fun resume() { log += "resume"; f.playing(true) }
    }

    private fun machine(grant: Boolean = true) = Fake(grant).let { p -> p to ClipFocus(p).also { p.f = it } }

    @Test fun playAsksOnceAndPauseGivesBack() {
        val (p, f) = machine()
        f.playing(true); f.playing(true); f.playing(true)
        f.playing(false)
        assertEquals(listOf("request", "abandon"), p.log)
        assertFalse(f.held)
    }

    @Test fun leavingGivesBackEvenWhileWaitingForAShortLoss() {
        val (p, f) = machine()
        f.playing(true)
        f.changed(AUDIOFOCUS_LOSS_TRANSIENT)
        f.stop()
        assertEquals(listOf("request", "pause", "abandon"), p.log)
        assertFalse(f.held); assertFalse(f.waiting)
    }

    @Test fun shortLossPausesKeepsTheRequestAndResumesOnGain() {
        val (p, f) = machine()
        f.playing(true)
        f.changed(AUDIOFOCUS_LOSS_TRANSIENT)
        assertTrue(f.held); assertTrue(f.waiting)
        f.changed(AUDIOFOCUS_GAIN)
        assertEquals(listOf("request", "pause", "resume"), p.log)
        assertTrue(f.held); assertFalse(f.waiting)
    }

    @Test fun duckingIsAPauseForSpeech() {
        val (p, f) = machine()
        f.playing(true)
        f.changed(AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        f.changed(AUDIOFOCUS_GAIN)
        assertEquals(listOf("request", "pause", "resume"), p.log)
    }

    @Test fun permanentLossPausesAndNeverResumes() {
        val (p, f) = machine()
        f.playing(true)
        f.changed(AUDIOFOCUS_LOSS)
        f.changed(AUDIOFOCUS_GAIN)
        assertEquals(listOf("request", "abandon", "pause"), p.log)
        assertFalse(f.held)
        // the student presses play again: a fresh request
        f.playing(true)
        assertEquals("request", p.log.last())
    }

    @Test fun deniedRequestDoesNotPlay() {
        val (p, f) = machine(grant = false)
        f.playing(true)
        assertEquals(listOf("request", "abandon", "pause"), p.log)
        assertFalse(f.held)
    }

    @Test fun playDuringAShortLossAsksAgain() {
        val (p, f) = machine()
        f.playing(true)
        f.changed(AUDIOFOCUS_LOSS_TRANSIENT)
        p.grant = false
        f.playing(true)
        assertEquals(listOf("request", "pause", "request", "abandon", "pause"), p.log)
        assertFalse(f.held); assertFalse(f.waiting)
    }

    @Test fun lossWhileNotPlayingDoesNothing() {
        val (p, f) = machine()
        f.changed(AUDIOFOCUS_LOSS_TRANSIENT)
        f.changed(AUDIOFOCUS_GAIN)
        assertEquals(emptyList<String>(), p.log)
    }
}
