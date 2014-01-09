/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.teamcity;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import jetbrains.buildServer.AgentRestrictor;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.StatusDescriptor;
import jetbrains.buildServer.issueTracker.Issue;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.BuildRevision;
import jetbrains.buildServer.serverSide.BuildStatistics;
import jetbrains.buildServer.serverSide.BuildStatisticsOptions;
import jetbrains.buildServer.serverSide.CompilationBlockBean;
import jetbrains.buildServer.serverSide.DownloadedArtifacts;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.ShortStatistics;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.comments.Comment;
import jetbrains.buildServer.serverSide.impl.RunningBuildState;
import jetbrains.buildServer.serverSide.userChanges.CanceledInfo;
import jetbrains.buildServer.serverSide.vcs.VcsLabel;
import jetbrains.buildServer.tests.TestInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.users.UserSet;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsRootInstanceEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ReportsMain {
    private static final String CLIENT_ID = "585858306678.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = System.getProperty("client.secret");
    private static final String OAUTH_TOKEN = System.getProperty("oauth.token");

    private static final Map<String, String> PARAMS;

    static {
        ReportsConstants constants = new ReportsConstants();

        PARAMS = new HashMap<>();
        PARAMS.put(constants.getApplicationClientId(), CLIENT_ID);
        PARAMS.put(constants.getApplicationClientSecret(), CLIENT_SECRET);
        PARAMS.put(constants.getApplicationOauthCode(), OAUTH_TOKEN);
    }

    private static final String BUILD_TYPE_ID = "AppEngineTck_Capedwarf";
    private static final String BUILD_NUMBER = "15";
    private static final Date BUILD_DATE = parse("09.01.2014 16:26");
    private static final long BUILD_DURATION = 1000 * 60 * 140; // 1h20min

    private static final int PASSED_TESTS_COUNT = 727;
    private static final int IGNORED_TESTS_COUNT = 9;
    private static final int FAILED_TESTS_COUNT = 8;

    private static Date parse(String string) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(string);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) {
        ReportsDescriptor descriptor = new ReportsDescriptorMock();
        ReportsConstants constants = new ReportsConstants();
        ReportsFeature feature = new ReportsFeature(null, descriptor, constants);
        SRunningBuild build = new SRunningBuildMock();
        SBuildFeatureDescriptor fd = new SBuildFeatureDescriptorMock();

        feature.handleTokens(PARAMS);
        feature.handleBuildFinished(build, fd);
    }

    private static class ReportsDescriptorMock extends ReportsDescriptor {
        private ReportsDescriptorMock() {
            super(null);
        }

        @NotNull
        @Override
        public String getFeaturePath() {
            return "";
        }

        @NotNull
        @Override
        public String getViewPath() {
            return "";
        }
    }

    private static class SRunningBuildMock implements SRunningBuild {
        @Override
        public String getCurrentPath() {
            return null;
        }

        @NotNull
        @Override
        public SBuildAgent getAgent() {
            return null;
        }

        @Nullable
        @Override
        public Integer getQueuedAgentId() {
            return null;
        }

        @Nullable
        @Override
        public AgentRestrictor getQueuedAgentRestrictor() {
            return null;
        }

        @Override
        public boolean isInterrupted() {
            return false;
        }

        @Override
        public int getSignature() {
            return 0;
        }

        @Override
        public void setSignature(int i) {

        }

        @Override
        public int getCompletedPercent() {
            return 0;
        }

        @Override
        public void addBuildMessages(@NotNull List<BuildMessage1> buildMessage1s) {

        }

        @Override
        public void addBuildMessage(@NotNull BuildMessage1 buildMessage1) {

        }

        @Override
        public void setBuildNumber(@NotNull String s) {

        }

        @Override
        public void setBuildStatus(Status status) {

        }

        @Override
        public void setInterrupted(@NotNull RunningBuildState runningBuildState, @Nullable User user, @Nullable String s) {

        }

        @Override
        public String getAgentAccessCode() {
            return null;
        }

        @Override
        public boolean isProbablyHanging() {
            return false;
        }

        @Override
        public boolean isOutdated() {
            return false;
        }

        @Override
        public Date getLastBuildActivityTimestamp() {
            return null;
        }

        @Override
        public long getTimeSpentSinceLastBuildActivity() {
            return 0;
        }

        @Override
        public void stop(@Nullable User user, @Nullable String s) {

        }

        @Override
        public long getEstimationForTimeLeft() {
            return 0;
        }

        @Override
        public long getDurationEstimate() {
            return 0;
        }

        @Override
        public long getDurationOvertime() {
            return 0;
        }

        @Override
        public long getElapsedTime() {
            return 0;
        }

        @NotNull
        @Override
        public File getArtifactsDirectory() {
            return null;
        }

        @NotNull
        @Override
        public BuildArtifacts getArtifacts(@NotNull BuildArtifactsViewMode buildArtifactsViewMode) {
            return null;
        }

        @NotNull
        @Override
        public List<SArtifactDependency> getArtifactDependencies() {
            return null;
        }

        @Override
        public boolean isArtifactsExists() {
            return false;
        }

        @Override
        public boolean isHasInternalArtifactsOnly() {
            return false;
        }

        @Override
        public boolean isResponsibleNeeded() {
            return false;
        }

        @NotNull
        @Override
        public BuildLog getBuildLog() {
            return null;
        }

        @NotNull
        @Override
        public ShortStatistics getShortStatistics() {
            return null;
        }

        @NotNull
        @Override
        public BuildStatistics getFullStatistics() {
            return new BuildStatisticsMock();
        }

        @NotNull
        @Override
        public BuildStatistics getBuildStatistics(@NotNull BuildStatisticsOptions buildStatisticsOptions) {
            return null;
        }

        @Nullable
        @Override
        public SUser getOwner() {
            return null;
        }

        @Override
        public TriggeredBy getTriggeredBy() {
            return null;
        }

        @Nullable
        @Override
        public SBuildType getBuildType() {
            return null;
        }

        @NotNull
        @Override
        public DownloadedArtifacts getDownloadedArtifacts() {
            return null;
        }

        @NotNull
        @Override
        public DownloadedArtifacts getProvidedArtifacts() {
            return null;
        }

        @Override
        public boolean isUsedByOtherBuilds() {
            return false;
        }

        @NotNull
        @Override
        public List<SVcsModification> getContainingChanges() {
            return null;
        }

        @NotNull
        @Override
        public List<SVcsModification> getChanges(SelectPrevBuildPolicy selectPrevBuildPolicy, boolean b) {
            return null;
        }

        @Override
        public UserSet<SUser> getCommitters(SelectPrevBuildPolicy selectPrevBuildPolicy) {
            return null;
        }

        @Override
        public boolean isOutOfChangesSequence() {
            return false;
        }

        @Override
        public List<String> getTags() {
            return null;
        }

        @Override
        public void setTags(List<String> strings) {

        }

        @Override
        public void setTags(User user, List<String> strings) {

        }

        @NotNull
        @Override
        public byte[] getFileContent(String s) throws VcsException {
            return new byte[0];
        }

        @Override
        public List<BuildRevision> getRevisions() {
            return null;
        }

        @Override
        public List<VcsLabel> getLabels() {
            return null;
        }

        @NotNull
        @Override
        public Date getQueuedDate() {
            return null;
        }

        @NotNull
        @Override
        public Date getServerStartDate() {
            return null;
        }

        @Override
        public List<VcsRootInstanceEntry> getVcsRootEntries() {
            return null;
        }

        @Nullable
        @Override
        public Date getClientStartDate() {
            return null;
        }

        @Override
        public boolean isStartedOnAgent() {
            return false;
        }

        @NotNull
        @Override
        public Date convertToServerTime(@NotNull Date date) {
            return null;
        }

        @NotNull
        @Override
        public Date convertToAgentTime(@NotNull Date date) {
            return null;
        }

        @Nullable
        @Override
        public String getBuildDescription() {
            return null;
        }

        @NotNull
        @Override
        public ValueResolver getValueResolver() {
            return null;
        }

        @Nullable
        @Override
        public Comment getBuildComment() {
            return null;
        }

        @Override
        public void setBuildComment(@Nullable User user, @Nullable String s) {

        }

        @Override
        public boolean isPinned() {
            return false;
        }

        @NotNull
        @Override
        public Collection<Issue> getRelatedIssues() {
            return null;
        }

        @Override
        public boolean isHasRelatedIssues() {
            return false;
        }

        @NotNull
        @Override
        public Map<String, String> getBuildOwnParameters() {
            return null;
        }

        @Override
        public String getRawBuildNumber() {
            return null;
        }

        @Override
        public boolean isInternalError() {
            return false;
        }

        @Nullable
        @Override
        public String getFirstInternalError() {
            return null;
        }

        @Nullable
        @Override
        public String getFirstInternalErrorMessage() {
            return null;
        }

        @Nullable
        @Override
        public TimeZone getClientTimeZone() {
            return null;
        }

        @Override
        public void addBuildProblem(@NotNull BuildProblemData buildProblemData) {

        }

        @Override
        public boolean hasBuildProblemOfType(@NotNull String s) {
            return false;
        }

        @NotNull
        @Override
        public List<BuildProblemData> getFailureReasons() {
            return null;
        }

        @Override
        public void muteBuildProblems(@NotNull User user, boolean b, @NotNull String s) {

        }

        @Override
        public BuildProblemData addUserBuildProblem(@NotNull User user, @NotNull String s) {
            return null;
        }

        @Nullable
        @Override
        public Branch getBranch() {
            return null;
        }

        @Nullable
        @Override
        public SFinishedBuild getPreviousFinished() {
            return null;
        }

        @Nullable
        @Override
        public BigDecimal getStatisticValue(String s) {
            return null;
        }

        @NotNull
        @Override
        public Map<String, BigDecimal> getStatisticValues() {
            return null;
        }

        @NotNull
        @Override
        public Date getStartDate() {
            return BUILD_DATE;
        }

        @Override
        public String getAgentName() {
            return null;
        }

        @Override
        public long getBuildId() {
            return 0;
        }

        @Override
        public StatusDescriptor getStatusDescriptor() {
            return null;
        }

        @Override
        public List<String> getLogMessages(int i, int i2) {
            return null;
        }

        @Override
        public List<TestInfo> getTestMessages(int i, int i2) {
            return null;
        }

        @Override
        public List<String> getCompilationErrorMessages() {
            return null;
        }

        @NotNull
        @Override
        public String getBuildTypeId() {
            return null;
        }

        @NotNull
        @Override
        public String getBuildTypeExternalId() {
            return BUILD_TYPE_ID;
        }

        @NotNull
        @Override
        public String getBuildTypeName() {
            return null;
        }

        @NotNull
        @Override
        public String getFullName() {
            return null;
        }

        @Nullable
        @Override
        public String getProjectId() {
            return null;
        }

        @Nullable
        @Override
        public String getProjectExternalId() {
            return null;
        }

        @Override
        public boolean isPersonal() {
            return false;
        }

        @Override
        public Status getBuildStatus() {
            return null;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public String getBuildNumber() {
            return BUILD_NUMBER;
        }

        @Nullable
        @Override
        public Date getFinishDate() {
            return null;
        }

        @Override
        public CanceledInfo getCanceledInfo() {
            return null;
        }

        @Override
        public long getDuration() {
            return BUILD_DURATION;
        }

        @NotNull
        @Override
        public BuildPromotion getBuildPromotion() {
            return null;
        }

        @Nullable
        @Override
        public SBuild getSequenceBuild() {
            return null;
        }

        @NotNull
        @Override
        public ParametersProvider getParametersProvider() {
            return null;
        }
    }

    private static class SBuildFeatureDescriptorMock implements SBuildFeatureDescriptor {
        @NotNull
        @Override
        public BuildFeature getBuildFeature() {
            return null;
        }

        @NotNull
        @Override
        public String getId() {
            return null;
        }

        @NotNull
        @Override
        public String getType() {
            return null;
        }

        @NotNull
        @Override
        public Map<String, String> getParameters() {
            return PARAMS;
        }
    }

    private static class BuildStatisticsMock implements BuildStatistics {
        @Override
        public List<STestRun> getIgnoredTests() {
            return Collections.emptyList();
        }

        @Override
        public List<STestRun> getPassedTests() {
            return Collections.emptyList();
        }

        @Override
        public List<CompilationBlockBean> getCompilationErrorBlocks() {
            return Collections.emptyList();
        }

        @Override
        public int getPreviousFailedTestsCount() {
            return 0;
        }

        @Override
        public long getTotalDuration() {
            return 0;
        }

        @Override
        public List<STestRun> getTests(@Nullable Status status, @NotNull Order order) {
            return Collections.emptyList();
        }

        @Override
        public List<STestRun> getAllTests() {
            return Collections.emptyList();
        }

        @Override
        public List<STestRun> findTestsBy(TestName testName) {
            return Collections.emptyList();
        }

        @Override
        public boolean isEmpty1() {
            return false;
        }

        @Override
        public Status getBuildStatus() {
            return null;
        }

        @Override
        public String getCurrentStage() {
            return null;
        }

        @Override
        public int getCompilationErrorsCount() {
            return 0;
        }

        @Override
        public int getPassedTestCount() {
            return PASSED_TESTS_COUNT;
        }

        @Override
        public int getNewFailedCount() {
            return 0;
        }

        @Override
        public int getIgnoredTestCount() {
            return IGNORED_TESTS_COUNT;
        }

        @Override
        public int getFailedTestCount() {
            return FAILED_TESTS_COUNT;
        }

        @Override
        public int getAllTestCount() {
            return 0;
        }

        @Override
        public List<STestRun> getFailedTests() {
            return Collections.emptyList();
        }

        @Override
        public List<STestRun> getFailedTestsIncludingMuted() {
            return null;
        }

        @Override
        public int getSignature() {
            return 0;
        }
    }
}
