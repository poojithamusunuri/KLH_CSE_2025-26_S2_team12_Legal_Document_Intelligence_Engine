#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/build.sh
for test in \
  tests.CorpusTrackIntegrationTest \
  tests.SearchSafetyRegressionTest \
  tests.SituationInterpreterTest \
  tests.ConceptGeneralizationRegressionTest \
  tests.TrapQueryRegressionTest \
  tests.QueryAnalysisIntegrationTest \
  tests.ProductionQueryRouteTest \
  tests.QueryFallbackTest \
  tests.Phase3LegalAnalysisTest \
  tests.Phase4ResearchPipelineTest \
  tests.AlgorithmCorrectnessTest \
  tests.Phase2SearchRoutingTest \
  tests.RelevanceAdversarialTest \
  tests.RelevanceBenchmarkTest \
  tests.OptimizerValidationTest \
  tests.RelevanceFocusedRegressionTest \
  tests.QueryConstraintAnalyzerTest \
  tests.CLIRegressionTest \
  tests.ScenarioEvaluation
do
  echo
  echo "===== $test ====="
  java -cp out "$test"
done
