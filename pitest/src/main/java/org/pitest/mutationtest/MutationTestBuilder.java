/*
 * Copyright 2011 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.MultipleTestGroup;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Prelude;
import org.pitest.mutationtest.instrument.KnownStatusMutationTestUnit;
import org.pitest.mutationtest.instrument.MutationTestUnit;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;

public class MutationTestBuilder {

  private final static Logger         LOG                                  = Log
                                                                               .getLogger();

  private final MutationSource mutationSource;
  private final MutationAnalyser analyser;
  private final ReportOptions         data;
  private final MutationConfig        mutationConfig;
  private final Configuration         configuration;
  private final JavaAgent             javaAgent;
  private final File baseDir;
  
  public MutationTestBuilder(final File baseDir, final MutationConfig mutationConfig, 
      final MutationSource mutationSource, final ReportOptions data, final Configuration configuration,
      final JavaAgent javaAgent) {
    this(baseDir, mutationConfig, new NullAnalyser(), mutationSource, data, configuration, javaAgent);
  }

  public MutationTestBuilder(final File baseDir, final MutationConfig mutationConfig, final MutationAnalyser analyser,
      final MutationSource mutationSource, final ReportOptions data, final Configuration configuration,
      final JavaAgent javaAgent) {
    this.data = data;
    this.mutationConfig = mutationConfig;
    this.mutationSource = mutationSource;
    this.analyser = analyser;
    this.configuration = configuration;
    this.javaAgent = javaAgent;
    this.baseDir = baseDir;
  }

  public List<TestUnit> createMutationTestUnits(
      final Collection<ClassName> codeClasses) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();

    for (final ClassName clazz : codeClasses) {
      final Collection<MutationDetails> mutationsForClasses = mutationSource.createMutations(clazz);
      if ( mutationsForClasses.isEmpty() ) {
        LOG.fine("No mutations found for " + clazz);
      } else {
        createMutationAnalysisUnits(tus, clazz, mutationsForClasses);
      }
    }
    return tus;
  }

  private void createMutationAnalysisUnits(final List<TestUnit> tus,
      final ClassName clazz,
      final Collection<MutationDetails> mutationsForClasses) {
    if (this.data.getMutationUnitSize() > 0) {
      final FunctionalList<List<MutationDetails>> groupedMutations = FCollection
          .splitToLength(this.data.getMutationUnitSize(), mutationsForClasses);
      FCollection.mapTo(groupedMutations, mutationDetailsToTestUnit(clazz), tus);
    } else {
      tus.add(createMutationTestUnit(mutationsForClasses));
    }
  }

  private F<List<MutationDetails>, TestUnit> mutationDetailsToTestUnit(final ClassName clazz) {
    return new F<List<MutationDetails>, TestUnit>() {
      public TestUnit apply(final List<MutationDetails> mutations) {
        return createMutationTestUnit(mutations);
      }
    };
  }

  private TestUnit createMutationTestUnit(
      final Collection<MutationDetails> mutationsForClasses) {

    Collection<MutationResult> analysedMutations = this.analyser.analyse(mutationsForClasses);
    
    Collection<MutationDetails> needAnalysis = FCollection.filter(analysedMutations, statusNotKnown()).map(resultToDetails());
    Collection<MutationResult> analysed = FCollection.filter(analysedMutations, Prelude.not(statusNotKnown()));
    
    if ( needAnalysis.isEmpty() ) {
      return makePreAnalysedUnit(analysed);
    }
    
    if ( analysed.isEmpty() ) {
      return makeUnanalysedUnit(needAnalysis);
    }
    
    return new MultipleTestGroup(Arrays.asList(makePreAnalysedUnit(analysed), makeUnanalysedUnit(needAnalysis)));

  }

  private TestUnit makePreAnalysedUnit(Collection<MutationResult> analysed) {
    return new KnownStatusMutationTestUnit(mutationConfig.getMutatorNames(), analysed);
  }

  private TestUnit makeUnanalysedUnit(Collection<MutationDetails> needAnalysis) {
    final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
    FCollection.flatMapTo(needAnalysis, mutationDetailsToTestClass(),
        uniqueTestClasses);

    return new MutationTestUnit(baseDir, needAnalysis, uniqueTestClasses,
        this.configuration, this.mutationConfig, this.javaAgent,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()), this.data.isVerbose(), this.data
            .getClassPath().getLocalClassPath());
  }

  private static F<MutationResult, MutationDetails> resultToDetails() {
    return new F<MutationResult, MutationDetails>() {
      public MutationDetails apply(MutationResult a) {
        return a.getDetails();
      }     
    };
  }

  private static F<MutationResult, Boolean> statusNotKnown() {
    return new F<MutationResult, Boolean>() {
      public Boolean apply(MutationResult a) {
        return a.getStatus() == DetectionStatus.NOT_STARTED;
      }
    };
  }

  private static F<MutationDetails, Iterable<ClassName>> mutationDetailsToTestClass() {
    return new F<MutationDetails, Iterable<ClassName>>() {
      public Iterable<ClassName> apply(final MutationDetails a) {
        return FCollection.map(a.getTestsInOrder(),
            TestInfo.toDefiningClassName());
      }
    };
  }

}
