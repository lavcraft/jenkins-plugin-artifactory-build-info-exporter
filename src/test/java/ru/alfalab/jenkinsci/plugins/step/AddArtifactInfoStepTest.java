package ru.alfalab.jenkinsci.plugins.step;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.Page;
import hudson.model.queue.QueueTaskFuture;
import lombok.Data;
import lombok.experimental.Delegate;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.xml.sax.SAXException;
import ru.alfalab.jenkinsci.plugins.ArtifactInfoData;
import ru.alfalab.jenkinsci.plugins.BakedArtifact;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author tolkv
 * @version 08/12/2017
 */
public class AddArtifactInfoStepTest {
  public static final ExternalAction   STUB_EXTERNAL_ACTION = new ExternalAction();
  private final       ObjectMapper     objectMapper         = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  @Rule public        JenkinsRule      jenkinsRule          = new JenkinsRule();

  @Test
  public void should_invoke_without_errors() throws Exception {
    //given
    String examplePipeline = "" +
        "def buildInfo = Artifactory.newBuildInfo()\n" +
        "def artifact = new org.jfrog.build.api.Artifact()\n" +
        "artifact.setType('jar')\n" +
        "artifact.setName('my-app-name')\n" +
        "def module = new org.jfrog.build.api.Module()\n" +
        "module.setArtifacts([artifact])\n" +
        "buildInfo.getModules().add(module)\n" +
        "appendArtifactInfo buildInfo";

    WorkflowJob workflowJob = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test_workflow");
    workflowJob.setDefinition(new CpsFlowDefinition(examplePipeline, true));
    QueueTaskFuture<WorkflowRun> workflowRunQueueTaskFuture = workflowJob.scheduleBuild2(0);

    //when
    BuildResponse buildResponse = getBuildInfoFromApi();

    //then
    jenkinsRule.assertBuildStatusSuccess(workflowRunQueueTaskFuture);

    assertTrue(
        buildResponse.actions.stream()
            .anyMatch(action -> action.className.equals("ru.alfalab.jenkinsci.plugins.ArtifactInfoData"))
    );

    BakedArtifact jarArtifact = buildResponse.actions.stream()
        .filter(action -> action.className.equals("ru.alfalab.jenkinsci.plugins.ArtifactInfoData"))
        .findAny()
        .orElse(STUB_EXTERNAL_ACTION)
        .getArtifactInfoData()
        .getArtifactsByType().get("jar").get(0);

    assertThat(jarArtifact.getName(),
        equalTo("my-app-name")
    );

    assertThat(jarArtifact.getType(),
        equalTo("jar")
    );
  }

  private BuildResponse getBuildInfoFromApi() throws IOException, SAXException {
    JenkinsRule.WebClient webClient       = jenkinsRule.createWebClient();
    Page                  page            = webClient.goTo("job/test_workflow/lastBuild/api/json?depth=4", "application/json");
    String                contentAsString = page.getWebResponse().getContentAsString();

    System.out.println("contentAsString = " + contentAsString);
    return objectMapper.readValue(contentAsString, BuildResponse.class);
  }


  @TestExtension
  public static class AddAllToWhitelist extends ProxyWhitelist {
    public AddAllToWhitelist() throws IOException {
      super(new StaticWhitelist(
          "method org.jfrog.hudson.pipeline.types.buildInfo.BuildInfo getModules",
          "new org.jfrog.build.api.Artifact",
          "method org.jfrog.build.api.BuildFileBean setType java.lang.String",
          "method org.jfrog.build.api.Artifact setName java.lang.String",
          "new org.jfrog.build.api.Module",
          "method org.jfrog.build.api.Module setArtifacts java.util.List"
      ));
    }
  }

  @Data
  public static class BuildResponse {
    private List<ExternalAction> actions;
  }

  @Data
  public static class ExternalAction {
    @JsonProperty("_class")
    private String className = "";

    @Delegate
    private ArtifactInfoData artifactInfoData = new ArtifactInfoData();

  }
}