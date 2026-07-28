package com.linkside.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TeeTimeLogicTest {

    private val user = User(
        id = "u1",
        phone = "+15551234567",
        firstName = "Jeff",
        lastName = "Olson",
    )

    private fun teeTime(
        inviteStatus: String,
        date: Instant,
        tripId: String? = null,
    ): TeeTime = TeeTime(
        id = "tt1",
        courseName = "Test Course",
        date = date.toString(),
        golfersNeeded = 4,
        creatorId = "host",
        tripId = tripId,
        invites = listOf(
            Invite(
                phone = "+15551234567",
                name = "Jeff Olson",
                status = inviteStatus,
                userId = "u1",
            ),
        ),
    )

    @Test
    fun isDeclinedBy_trueWhenNo() {
        val tt = teeTime(InviteStatus.NO.raw, Instant.now().plus(2, ChronoUnit.DAYS))
        assertTrue(tt.isDeclinedBy(user))
    }

    @Test
    fun isDeclinedBy_falseWhenYes() {
        val tt = teeTime(InviteStatus.YES.raw, Instant.now().plus(2, ChronoUnit.DAYS))
        assertFalse(tt.isDeclinedBy(user))
    }

    @Test
    fun isActiveDeclined_trueForUpcomingDeclined() {
        val tt = teeTime(InviteStatus.NO.raw, Instant.now().plus(1, ChronoUnit.DAYS))
        assertTrue(tt.isActiveDeclined(user))
    }

    @Test
    fun isActiveDeclined_falseMoreThan24HoursAfterTeeTime() {
        val tt = teeTime(InviteStatus.NO.raw, Instant.now().minus(25, ChronoUnit.HOURS))
        assertFalse(tt.isActiveDeclined(user))
    }

    @Test
    fun isActiveDeclined_trueWithin24HoursAfterTeeTime() {
        val tt = teeTime(InviteStatus.NO.raw, Instant.now().minus(12, ChronoUnit.HOURS))
        assertTrue(tt.isActiveDeclined(user))
    }

    @Test
    fun homeFilter_hidesDeclinedButProfileKeepsActiveDeclined() {
        val declined = teeTime(InviteStatus.NO.raw, Instant.now().plus(2, ChronoUnit.DAYS))
        val accepted = teeTime(InviteStatus.YES.raw, Instant.now().plus(2, ChronoUnit.DAYS))
        val all = listOf(declined, accepted)

        val home = all.filter { !it.isDeclinedBy(user) }
        val profile = all.filter { it.isActiveDeclined(user) }

        assertEquals(1, home.size)
        assertEquals(accepted.id, home.first().id)
        assertEquals(1, profile.size)
        assertEquals(declined.id, profile.first().id)
    }

    @Test
    fun withInviteStatusFor_marksActiveDeclinedImmediately() {
        val upcoming = teeTime(InviteStatus.YES.raw, Instant.now().plus(2, ChronoUnit.DAYS))
        assertFalse(upcoming.isActiveDeclined(user))

        val declined = upcoming.withInviteStatusFor(
            user = user,
            phone = user.phone,
            userId = user.id,
            status = InviteStatus.NO,
        )
        assertTrue(declined.isDeclinedBy(user))
        assertTrue(declined.isActiveDeclined(user))
    }

    @Test
    fun tripTeeTime_hasTripId() {
        val tt = teeTime(InviteStatus.YES.raw, Instant.now().plus(1, ChronoUnit.DAYS), tripId = "trip1")
        assertTrue(!tt.tripId.isNullOrBlank())
    }
}

class GolfTripDeclinedLogicTest {
    private val user = User(
        id = "u1",
        phone = "+15551234567",
        firstName = "Jeff",
        lastName = "Olson",
    )

    private fun trip(
        inviteStatus: String,
        end: Instant,
        start: Instant = end.minus(2, ChronoUnit.DAYS),
    ): GolfTrip = GolfTrip(
        id = "trip1",
        name = "Pebble Weekend",
        location = "Pebble Beach",
        startDate = start.toEpochMilli().toDouble(),
        endDate = end.toEpochMilli().toDouble(),
        golfersNeeded = 8,
        creatorId = "host",
        invites = listOf(
            Invite(
                phone = "+15551234567",
                name = "Jeff Olson",
                status = inviteStatus,
                userId = "u1",
            ),
        ),
    )

    @Test
    fun isActiveDeclined_trueForUpcomingDeclined() {
        val t = trip(InviteStatus.NO.raw, Instant.now().plus(3, ChronoUnit.DAYS))
        assertTrue(t.isActiveDeclined(user))
    }

    @Test
    fun isActiveDeclined_falseMoreThan24HoursAfterEnd() {
        val t = trip(InviteStatus.NO.raw, Instant.now().minus(25, ChronoUnit.HOURS))
        assertFalse(t.isActiveDeclined(user))
    }

    @Test
    fun homeFilter_hidesDeclinedButProfileKeepsActiveDeclined() {
        val declined = trip(InviteStatus.NO.raw, Instant.now().plus(5, ChronoUnit.DAYS))
        val accepted = trip(InviteStatus.YES.raw, Instant.now().plus(5, ChronoUnit.DAYS)).copy(id = "trip2")
        val all = listOf(declined, accepted)

        val home = all.filter { !it.isDeclinedBy(user) }
        val profile = all.filter { it.isActiveDeclined(user) }

        assertEquals(1, home.size)
        assertEquals(accepted.id, home.first().id)
        assertEquals(1, profile.size)
        assertEquals(declined.id, profile.first().id)
    }

    @Test
    fun withInviteStatusFor_marksActiveDeclinedImmediately() {
        val upcoming = trip(InviteStatus.YES.raw, Instant.now().plus(5, ChronoUnit.DAYS))
        assertFalse(upcoming.isActiveDeclined(user))

        val declined = upcoming.withInviteStatusFor(user, InviteStatus.NO)
        assertTrue(declined.isDeclinedBy(user))
        assertTrue(declined.isActiveDeclined(user))
    }
}

class ContactStatusTest {
    @Test
    fun isOnLinkside_whenRegistered() {
        assertTrue(ContactStatus(registered = true, optedIn = false).isOnLinkside)
    }

    @Test
    fun isOnLinkside_whenOptedInOnly() {
        assertTrue(ContactStatus(registered = false, optedIn = true).isOnLinkside)
    }

    @Test
    fun isOnLinkside_falseWhenNeither() {
        assertFalse(ContactStatus(registered = false, optedIn = false).isOnLinkside)
    }
}

class ScoringEngineTest {
    private fun hole(n: Int, score: Int, par: Int = 4) =
        ScorecardHole(hole = n, score = score, par = par)

    private fun card(id: String, name: String, scores: List<Int>) = TeeTimeScorecard(
        id = id,
        teeTimeId = "tt",
        uploadedBy = "u",
        playerName = name,
        holes = scores.mapIndexed { i, s -> hole(i + 1, s) },
        total = scores.sum(),
        source = "manual",
    )

    @Test
    fun strokePlay_sortsByTotal() {
        val rows = ScoringEngine.leaderboard(
            scorecards = listOf(
                card("1", "Bob", listOf(5, 5, 5)),
                card("2", "Ann", listOf(4, 4, 4)),
            ),
            playFormat = PlayFormat.STROKE_PLAY.raw,
            holes = 3,
        )
        assertEquals(listOf("Ann", "Bob"), rows.map { it.name })
        assertEquals(12, rows[0].total)
        assertEquals(15, rows[1].total)
    }

    @Test
    fun scramble_collapsesToTeamBestBallPerHole() {
        val rows = ScoringEngine.leaderboard(
            scorecards = listOf(
                card("1", "A", listOf(5, 3, 5)),
                card("2", "B", listOf(4, 4, 4)),
            ),
            playFormat = PlayFormat.SCRAMBLE.raw,
            holes = 3,
            teamName = "Birdie Boys",
        )
        assertEquals(1, rows.size)
        assertTrue(rows[0].isTeam)
        assertEquals("Birdie Boys", rows[0].name)
        // best per hole: 4, 3, 4 = 11
        assertEquals(11, rows[0].total)
    }

    @Test
    fun ignoresSpecsAndEmptyCards() {
        val specs = TeeTimeScorecard(
            id = "s",
            playerName = "_specs_",
            source = "manual",
            holes = listOf(hole(1, 0, 4)),
            total = 0,
        )
        val empty = TeeTimeScorecard(
            id = "e",
            playerName = "Empty",
            source = "manual",
            holes = emptyList(),
            total = 0,
        )
        val rows = ScoringEngine.leaderboard(
            scorecards = listOf(specs, empty, card("1", "Real", listOf(4, 4))),
            playFormat = PlayFormat.STROKE_PLAY.raw,
            holes = 2,
        )
        assertEquals(1, rows.size)
        assertEquals("Real", rows[0].name)
    }
}
