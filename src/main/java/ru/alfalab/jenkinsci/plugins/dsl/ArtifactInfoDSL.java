package ru.alfalab.jenkinsci.plugins.dsl;

import hudson.Extension;
import org.jenkinsci.plugins.pipelinedsl.PipelineDSLGlobal;

/**
 * @author tolkv
 * @version 08/12/2017
 */
@Extension
public class ArtifactInfoDSL extends PipelineDSLGlobal {
  public static final String ARTIFACT_FUNCTION_NAME_INFO = "artifactInfo";

  @Override
  public String getFunctionName() {
    return ARTIFACT_FUNCTION_NAME_INFO;
  }
}
