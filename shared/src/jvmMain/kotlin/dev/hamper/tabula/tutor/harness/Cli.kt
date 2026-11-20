package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.tutor.Pripojenie
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.Zaznam
import dev.hamper.tabula.tutor.siwc.HttpResponses
import dev.hamper.tabula.tutor.siwc.Siwc
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.system.exitProcess

/*
 * The tutor's harness on the Mac (docs/TUTOR.md, "Harness"): the same sign-in, client and pipeline as the phone, with a
 * file vault instead of the Keystore. Run through scripts/tutor.sh, which needs no Gradle once built:
 *
 *   scripts/tutor.sh login            prints the sign-in URL, waits for the browser's callback on 127.0.0.1
 *   scripts/tutor.sh login --url URL  finishes a sign-in with the callback URL pasted from the address bar
 *   scripts/tutor.sh stav | modely | odhlas
 *   scripts/tutor.sh sonda …          what the route accepts (reasoning effort, json_schema with an image, tokens per page)
 *   scripts/tutor.sh citanie …        reading photos with known content (Č)
 *   scripts/tutor.sh vysvetlenie …    explanations of generated verdicts (V)
 */

val logCli = Zaznam { u, t -> System.err.println("[$u] $t") }

fun harnessSiwc() = Siwc(SuborovyTrezor(), "FITagoras harness", null, logCli)

fun main(args: Array<String>) {
    val cmd = args.firstOrNull() ?: "help"
    val rest = args.drop(1)
    val code = try {
        when (cmd) {
            "login" -> login(rest)
            "stav" -> { println(stav()); 0 }
            "modely" -> runBlocking { HttpResponses(harnessSiwc(), logCli).modely().forEach(::println); 0 }
            "obnov" -> runBlocking {
                // a forced refresh: the rotated refresh token is saved before the new access token is used
                val pred = harnessSiwc().trezor.nacitaj().optString("refresh_token").hashCode()
                harnessSiwc().obnov(vynutene = true)
                val po = harnessSiwc().trezor.nacitaj().optString("refresh_token").hashCode()
                println("Obnovené; refresh token ${if (pred != po) "sa vymenil (rotácia)" else "ostal rovnaký"}; ${HttpResponses(harnessSiwc(), logCli).modely().size} modelov"); 0
            }
            "odhlas" -> { println(if (runBlocking { harnessSiwc().odhlas() }) "Odpojené, revokácia potvrdená." else "Tokeny zmazané, revokácia nepotvrdená."); 0 }
            "sonda" -> Sonda.main(rest)
            "citanie" -> Citanie.main(rest)
            "vysvetlenie" -> Vysvetlenie.main(rest)
            "data" -> Data.main(rest)
            "papier" -> Papier.main(rest)
            "tutor" -> Tutor.main(rest)
            "realne" -> Realne.main(rest)
            else -> { System.err.println("Príkazy: login [--url URL] [--suhlas] [--cakaj s], stav, modely, obnov, odhlas, sonda, citanie, vysvetlenie, data"); 2 }
        }
    } catch (e: TutorChyba) {
        System.err.println("CHYBA " + e.popis()); 1
    }
    exitProcess(code)
}

private fun stav(): String = when (val s = harnessSiwc().stav()) {
    Pripojenie.Nepripojeny, Pripojenie.Pripaja -> "nepripojený"
    is Pripojenie.Pripojeny -> "pripojený: ${s.email ?: "?"}, plán ${if (s.planPovoleny) "povolený" else "NEPOVOLENÝ (chýba scope ${Siwc.SHARING})"}"
}

private fun login(args: List<String>): Int {
    val siwc = harnessSiwc()
    val url = args.getOrNull(args.indexOf("--url") + 1)?.takeIf { "--url" in args }
    if (url != null) {
        val (pokus, cb) = siwc.callbackZUrl(url)
        po(runBlocking { siwc.dokonci(pokus, cb) })
        return 0
    }
    val cakaj = args.getOrNull(args.indexOf("--cakaj") + 1)?.takeIf { "--cakaj" in args }?.toLongOrNull() ?: 3600L
    val (posluchac, authUrl) = siwc.pripravPrihlasenie(suhlas = "--suhlas" in args)
    val port = posluchac.port
    println(
        """
        |Prihlásenie harnessu FITagoras cez ChatGPT
        |
        |1. Otvor túto adresu v prehliadači NA TOMTO MACU (alebo na inom, pozri 2):
        |
        |$authUrl
        |
        |2. Z iného počítača: najprv v jeho termináli  ssh -N -L $port:127.0.0.1:$port <tento počítač>  a potom adresu otvor tam.
        |3. Prihlás sa a povoľ používanie plánu. Stránka „Prihlásenie prijaté“ = hotovo.
        |   Ak sa stránka nenačíta, skopíruj adresu z adresného riadka a spusti: scripts/tutor.sh login --url '<adresa>'
        |
        |Čakám na callback na 127.0.0.1:$port (najviac $cakaj s)…
        """.trimMargin()
    )
    File(System.getProperty("user.home"), ".fitagoras").let { d ->
        // the URL also goes to a file, so whoever relays it doesn't have to dig it out of a log
        if (d.exists() || d.mkdirs()) runCatching {
            val f = File(d, "prihlasenie-url.txt"); f.writeText(authUrl + "\n")
            java.nio.file.Files.setPosixFilePermissions(f.toPath(), java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"))
        }
    }
    // the browser's callback, or the same sign-in finished by `login --url` in another process (the pending attempt
    // then leaves the vault): whichever comes first
    val koniec = System.currentTimeMillis() + cakaj * 1000
    var cb: Result<Siwc.Callback>? = null
    var inde = false
    runBlocking {
        while (cb == null && System.currentTimeMillis() < koniec) {
            cb = withTimeoutOrNull(2000) { runCatching { posluchac.vysledok.await() } }
            val pend = runCatching { siwc.trezor.nacitaj().optJSONObject("pending")?.optString("state") }.getOrNull()
            if (cb == null && pend != posluchac.pokus.state) { inde = true; break }
        }
    }
    posluchac.zavri()
    File(System.getProperty("user.home"), ".fitagoras/prihlasenie-url.txt").delete()
    if (inde) { println("Prihlásenie dokončené inde (login --url): ${stav()}"); return 0 }
    val c = cb ?: run { siwc.zrusPokus(posluchac.pokus); System.err.println("Čas vypršal, nikto sa neprihlásil."); return 3 }
    val plan = try { runBlocking { siwc.dokonci(posluchac.pokus, c.getOrElse { throw it }) } } catch (e: Exception) { siwc.zrusPokus(posluchac.pokus); throw e }
    po(plan)
    return 0
}

/** What a finished sign-in prints: the account, the plan, and one real call (the models it may use). */
private fun po(plan: Boolean) {
    println("Prihlásené: ${stav()}")
    if (!plan) { println("Plán nie je povolený: spusti  scripts/tutor.sh login --suhlas"); return }
    val m = runCatching { runBlocking { HttpResponses(harnessSiwc(), logCli).modely() } }
    m.onSuccess { println("Overené volaním API: ${it.size} modelov (${it.joinToString()})") }
        .onFailure { println("Volanie API zlyhalo: ${(it as? TutorChyba)?.popis() ?: it.javaClass.simpleName}") }
}
