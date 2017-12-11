package ru.alfalab.jenkinsci.plugins.step;

import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.model.Run;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.*;
import org.jfrog.build.api.Artifact;
import org.jfrog.hudson.pipeline.types.buildInfo.BuildInfo;
import org.kohsuke.stapler.DataBoundConstructor;
import ru.alfalab.jenkinsci.plugins.ArtifactInfoData;
import ru.alfalab.jenkinsci.plugins.BakedArtifact;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.*;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author tolkv
 * @version 08/12/2017
 */
@Getter
@Setter
public class AddArtifactInfoStep extends Step {
  private final Map<String, List<BakedArtifact>> artifactsByType;

  @DataBoundConstructor
  public AddArtifactInfoStep(BuildInfo buildInfo) {
    artifactsByType = buildInfo.getModules().stream()
        .flatMap(module -> ofNullable(module.getArtifacts())
            .orElse(emptyList())
            .stream()
            .map(artifact -> BakedArtifact.builder()
                .name(artifact.getName())
                .sha1(artifact.getSha1())
                .md5(artifact.getMd5())
                .type(artifact.getType())
                .build()))
        .collect(groupingBy(BakedArtifact::getType));
  }

  @Override
  public StepExecution start(StepContext context) {
    return new Execution(context, this);
  }

  public static class Execution extends SynchronousNonBlockingStepExecution<Void> {
    private static final     long   serialVersionUID = 1L;
    private transient static Logger logger           = Logger.getLogger("AddArtifactInfoStep");
    private transient final AddArtifactInfoStep step;
    private transient final Run                 build;

    Execution(@Nonnull StepContext context, AddArtifactInfoStep step) {
      super(context);

      try {
        this.step = step;
        this.build = context.get(Run.class);
      } catch (IOException | InterruptedException e) {
        logger.log(INFO, e, () -> "Not found step and build in context. With message: " + e.getLocalizedMessage());
        throw new IllegalStateException("Not found step and build in context");
      }

    }


    @Override
    protected Void run() throws Exception {

      build.addAction(
          new ArtifactInfoData(step.getArtifactsByType())
      );

      return null;
    }
  }


  @Extension
  public static final class DescriptorImpl extends StepDescriptor {

    @Override
    public Set<? extends Class<?>> getRequiredContext() {
      return Sets.<Class<?>>newHashSet(Run.class);
    }

    @Override
    public String getFunctionName() {
      return "appendArtifactInfo";
    }

    @Override
    public String getDisplayName() {
      return "Append information about artifact";
    }

    @Override
    public boolean isAdvanced() {
      return true;
    }
  }

}
