// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.rules.rewrite.logical;

import org.apache.doris.nereids.analyzer.UnboundRelation;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.literal.IntegerLiteral;
import org.apache.doris.nereids.trees.plans.logical.LogicalPlan;
import org.apache.doris.nereids.trees.plans.logical.RelationUtil;
import org.apache.doris.nereids.util.LogicalPlanBuilder;
import org.apache.doris.nereids.util.MemoPatternMatchSupported;
import org.apache.doris.nereids.util.PlanChecker;
import org.apache.doris.qe.ConnectContext;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MergeFilters}.
 */
class MergeFiltersTest implements MemoPatternMatchSupported {
    @Test
    void testMergeFilters() {
        Expression expression1 = new IntegerLiteral(1);
        Expression expression2 = new IntegerLiteral(2);
        Expression expression3 = new IntegerLiteral(3);

        LogicalPlan logicalFilter = new LogicalPlanBuilder(
                new UnboundRelation(RelationUtil.newRelationId(), Lists.newArrayList("db", "table")))
                .filter(ImmutableSet.of(expression1))
                .filter(ImmutableSet.of(expression2))
                .filter(ImmutableSet.of(expression3))
                .build();

        PlanChecker.from(new ConnectContext(), logicalFilter).applyBottomUp(new MergeFilters())
                .matches(
                        logicalFilter(
                                unboundRelation()
                        ).when(filter -> filter.getConjuncts()
                                .equals(ImmutableSet.of(expression1, expression2, expression3))));
    }
}
