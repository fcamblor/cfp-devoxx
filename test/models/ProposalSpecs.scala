/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Association du Paris Java User Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package models

import org.apache.commons.lang3.RandomStringUtils
import play.api.test.{FakeApplication, PlaySpecification, WithApplication}

/**
 * Play uses Specs2 for testing.
 *
 * Author: nicolas
 * Created: 14/01/2014 12:17
 */
class ProposalSpecs extends PlaySpecification {
  val testRedis = Map(
    "redis.host" -> "localhost"
    , "redis.port" -> "6363"
    , "redis.activeDatabase" -> 1
  )
  val appWithTestRedis = FakeApplication(additionalConfiguration = testRedis)

  val sampleProposalType = ConferenceDescriptor.ConferenceProposalTypes

  "correctly update the track when we change one proposal track to another" in new WithApplication(appWithTestRedis) {

    val newProposal = new Proposal(id = "TST-000", "DV", "fr", "title", "1234",
      None, Nil,
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save("123", newProposal, ProposalState.SUBMITTED)

    val Some(proposal) = Proposal.findById("TST-000")
    proposal.id must equalTo("TST-000")
    proposal.track must equalTo(Track.parse("java"))
    proposal.state must equalTo(ProposalState.SUBMITTED)

    // Get total of votes for JAVA
    val totalSubmitted = Proposal.totalSubmittedByTrack()
    val totalJava = totalSubmitted.find(p => p._1 == Track.parse("java"))
    val totalFuture = totalSubmitted.find(p => p._1 == Track.parse("future"))
    val totalJavaDepart = totalJava.map(_._2).getOrElse(0)
    val totalFutureDepart = totalFuture.map(_._2).getOrElse(0)

    Proposal.changeTrack("1234", proposal.copy(track = Track.parse("future")))
    val totalSubmitted2 = Proposal.totalSubmittedByTrack()
    val totalJava2 = totalSubmitted2.find(p => p._1 == Track.parse("java"))
    val totalFuture2 = totalSubmitted2.find(p => p._1 == Track.parse("future"))
    totalJava2.map(_._2).getOrElse(0) must equalTo(totalJavaDepart - 1)
    totalFuture2.map(_._2).getOrElse(0) must equalTo(totalFutureDepart + 1)

    Proposal.changeTrack("1234", proposal.copy(track = Track.parse("future")))
    val totalSubmitted3 = Proposal.totalSubmittedByTrack()
    val totalJava3 = totalSubmitted3.find(p => p._1 == Track.parse("java"))
    val totalFuture3 = totalSubmitted3.find(p => p._1 == Track.parse("future"))
    totalJava3.map(_._2).getOrElse(0) must equalTo(totalJavaDepart - 1)
    totalFuture3.map(_._2).getOrElse(0) must equalTo(totalFutureDepart + 1)

    Proposal.changeTrack("1234", proposal.copy(track = Track.parse("java")))
    val totalSubmitted4 = Proposal.totalSubmittedByTrack()
    val totalJavaFinal = totalSubmitted4.find(p => p._1 == Track.parse("java"))
    val totalFuture4 = totalSubmitted4.find(p => p._1 == Track.parse("future"))
    totalJavaFinal.map(_._2).getOrElse(0) must equalTo(totalJavaDepart)
    totalFuture4.map(_._2).getOrElse(0) must equalTo(totalFutureDepart)

    Proposal.delete("1234", "TST-000")
<<<<<<< HEAD
  }

  "add a 2n speaker on an existing Proposal" in new WithApplication(appWithTestRedis) {

    // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val secondarySpeaker = "speaker2-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(5)

    val newProposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      None, Nil,
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save(mainSpeaker, newProposal, ProposalState.ACCEPTED)


    // WHEN
    Proposal.updateSecondarySpeaker(mainSpeaker, proposalId, None, Some(secondarySpeaker))

    // THEN
    val updated = Proposal.findById(proposalId).get
    updated.mainSpeaker must equalTo(mainSpeaker)
    updated.secondarySpeaker must equalTo(Some(secondarySpeaker))
    Proposal.allAcceptedForSpeaker(secondarySpeaker) mustEqual List(updated)
    updated.state mustEqual ProposalState.ACCEPTED
  }

  "update the 2nd speaker on an existing Proposal should remove talk from old 2nd speaker (bug #165)" in new WithApplication(appWithTestRedis) {
    // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val secondarySpeaker = "speakerOLD-" + RandomStringUtils.randomAlphabetic(3)
    val newSecondarySpeaker = "speakerNEW-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(5)

    val newProposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      Option(secondarySpeaker),
      Nil,
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save(mainSpeaker, newProposal, ProposalState.SUBMITTED)

    // WHEN
    Proposal.updateSecondarySpeaker(mainSpeaker, proposalId, Some(secondarySpeaker), Some(newSecondarySpeaker))

    // THEN
    val updated = Proposal.findById(proposalId).get
    updated.mainSpeaker must equalTo(mainSpeaker)
    updated.secondarySpeaker must equalTo(Some(newSecondarySpeaker))
    Proposal.allProposalsByAuthor(secondarySpeaker).get(proposalId) must beNone
    Proposal.allProposalsByAuthor(newSecondarySpeaker).get(proposalId) must beSome[Proposal]
  }

  "move a speaker from otherSpeaker to 2nd speaker" in new WithApplication(appWithTestRedis) {
    // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val otherSpeaker1 = "speaker2-" + RandomStringUtils.randomAlphabetic(3)
    val otherSpeaker2 = "speakerNEW-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(5)

    val newProposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      None, // No 2nd speaker
      List(otherSpeaker1, otherSpeaker2),
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save(mainSpeaker, newProposal, ProposalState.SUBMITTED)

    // WHEN
    Proposal.updateSecondarySpeaker(mainSpeaker, proposalId, None, Some(otherSpeaker1))

    // THEN
    val updated = Proposal.findById(proposalId).get
    updated.mainSpeaker must equalTo(mainSpeaker)
    updated.secondarySpeaker must equalTo(Some(otherSpeaker1))
  }

  "add a new otherSpeaker" in new WithApplication(appWithTestRedis) {
    // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val otherSpeaker1 = "speaker-other-new-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(5)

    val newProposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      None, // No 2nd speaker
      Nil,
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save(mainSpeaker, newProposal, ProposalState.SUBMITTED)

    // WHEN
    val original = Proposal.findById(proposalId).get
    original.otherSpeakers must equalTo(Nil)

    Proposal.updateOtherSpeakers(mainSpeaker, proposalId, Nil, List(otherSpeaker1))

    // THEN
    val updated = Proposal.findById(proposalId).get
    updated.mainSpeaker must equalTo(mainSpeaker)
    updated.otherSpeakers must equalTo(List(otherSpeaker1))
  }

   "remove an otherSpeaker" in new WithApplication(appWithTestRedis) {
    // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val otherSpeaker1 = "speaker-other-new-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(5)

    val newProposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      None, // No 2nd speaker
      List(otherSpeaker1),
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save(mainSpeaker, newProposal, ProposalState.SUBMITTED)

    // WHEN
    val original = Proposal.findById(proposalId).get
    original.otherSpeakers must equalTo(List(otherSpeaker1))

    Proposal.updateOtherSpeakers(mainSpeaker, proposalId, List(otherSpeaker1),Nil)

    // THEN
    val updated = Proposal.findById(proposalId).get
    updated.otherSpeakers must equalTo(Nil)

    Proposal.allProposalsByAuthor(otherSpeaker1).get(proposalId) must beNone

  }

  "remove from a list one otherSpeaker" in new WithApplication(appWithTestRedis) {
    // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val otherSpeaker1 = "speaker-other-new-" + RandomStringUtils.randomAlphabetic(3)
    val otherSpeaker2 = "speaker-other-new-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(5)

    val newProposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      None, // No 2nd speaker
      List(otherSpeaker1,otherSpeaker2),
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

    Proposal.save(mainSpeaker, newProposal, ProposalState.SUBMITTED)

    // WHEN
    val original = Proposal.findById(proposalId).get

    Proposal.updateOtherSpeakers(mainSpeaker, proposalId, List(otherSpeaker1,otherSpeaker2), List(otherSpeaker2))

    // THEN
    val updated = Proposal.findById(proposalId).get
    updated.otherSpeakers must equalTo(List(otherSpeaker2))
    Proposal.allProposalsByAuthor(otherSpeaker1).get(proposalId) must beNone
  }

  "update the list of Accepted speakers if we remove the secondary speaker and it has no more talks" in new WithApplication(appWithTestRedis) {
      // GIVEN
    val mainSpeaker = "speaker-" + RandomStringUtils.randomAlphabetic(2)
    val otherSpeaker1 = "speaker-other-new-" + RandomStringUtils.randomAlphabetic(3)
    val proposalId = "TST-" + RandomStringUtils.randomAlphabetic(8)

      val proposal = new Proposal(id = proposalId, "DV", "fr", "title",
      mainSpeaker,
      Some(otherSpeaker1),
      Nil,
      sampleProposalType.CONF, "audience",
      "summary", "generated by tests",
      ProposalState.UNKNOWN, sponsorTalk = false,
      Track.parse("java"), Some("d1"),
      userGroup = None,
      wishlisted = Some(false))

      Proposal.save(mainSpeaker,proposal,ProposalState.ACCEPTED)
      val correctProposal = Proposal.findById(proposal.id).get // Cause we need the correct speakerId
      ApprovedProposal.approve(correctProposal)

      // WHEN
      ApprovedProposal.allAcceptedTalksForSpeaker(otherSpeaker1).toList.flatMap(_.secondarySpeaker) mustEqual List(otherSpeaker1)
      Proposal.updateSecondarySpeaker(mainSpeaker, proposalId, Some(otherSpeaker1),None)

      ApprovedProposal.allAcceptedTalksForSpeaker(otherSpeaker1).toList must be(Nil)


=======

    val Some(proposalDel) = Proposal.findById("TST-000")
    proposalDel.id must equalTo("TST-000")
    proposalDel.track must equalTo(Track.parse("java"))
    proposalDel.state must equalTo(ProposalState.DELETED)
>>>>>>> test(proposal): More tests for proposals
  }
}
