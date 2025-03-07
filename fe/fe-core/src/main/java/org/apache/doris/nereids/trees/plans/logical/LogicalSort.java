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

package org.apache.doris.nereids.trees.plans.logical;

import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.properties.OrderKey;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.Slot;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.algebra.Sort;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;
import org.apache.doris.nereids.util.Utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Logical Sort plan.
 * <p>
 * eg: select * from table order by a, b desc;
 * orderKeys: list of column information after order by. eg:[a, asc],[b, desc].
 * OrderKey: Contains order expression information and sorting method. Default is ascending.
 */
public class LogicalSort<CHILD_TYPE extends Plan> extends LogicalUnary<CHILD_TYPE> implements Sort {

    private final List<OrderKey> orderKeys;

    private final boolean orderKeysPruned;

    public LogicalSort(List<OrderKey> orderKeys, CHILD_TYPE child) {
        this(orderKeys, Optional.empty(), Optional.empty(), child);
    }

    public LogicalSort(List<OrderKey> orderKeys, CHILD_TYPE child, boolean orderKeysPruned) {
        this(orderKeys, Optional.empty(), Optional.empty(), child, orderKeysPruned);
    }

    /**
     * Constructor for LogicalSort.
     */
    public LogicalSort(List<OrderKey> orderKeys, Optional<GroupExpression> groupExpression,
            Optional<LogicalProperties> logicalProperties, CHILD_TYPE child) {
        this(orderKeys, groupExpression, logicalProperties, child, false);
    }

    public LogicalSort(List<OrderKey> orderKeys, Optional<GroupExpression> groupExpression,
            Optional<LogicalProperties> logicalProperties, CHILD_TYPE child, boolean orderKeysPruned) {
        super(PlanType.LOGICAL_SORT, groupExpression, logicalProperties, child);
        this.orderKeys = ImmutableList.copyOf(Objects.requireNonNull(orderKeys, "orderKeys can not be null"));
        this.orderKeysPruned = orderKeysPruned;
    }

    @Override
    public List<Slot> computeOutput() {
        return child().getOutput();
    }

    public List<OrderKey> getOrderKeys() {
        return orderKeys;
    }

    public boolean isOrderKeysPruned() {
        return orderKeysPruned;
    }

    @Override
    public String toString() {
        return Utils.toSqlString("LogicalSort[" + id.asInt() + "]",
                "orderKeys", orderKeys);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogicalSort that = (LogicalSort) o;
        return Objects.equals(orderKeys, that.orderKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderKeys);
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitLogicalSort(this, context);
    }

    @Override
    public List<? extends Expression> getExpressions() {
        return orderKeys.stream()
                .map(OrderKey::getExpr)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public LogicalSort<Plan> withChildren(List<Plan> children) {
        Preconditions.checkArgument(children.size() == 1);
        return new LogicalSort<>(orderKeys, children.get(0), orderKeysPruned);
    }

    @Override
    public LogicalSort<Plan> withGroupExpression(Optional<GroupExpression> groupExpression) {
        return new LogicalSort<>(orderKeys, groupExpression, Optional.of(getLogicalProperties()), child(),
                orderKeysPruned);
    }

    @Override
    public LogicalSort<Plan> withLogicalProperties(Optional<LogicalProperties> logicalProperties) {
        return new LogicalSort<>(orderKeys, Optional.empty(), logicalProperties, child(), false);
    }

    public LogicalSort<Plan> withOrderKeys(List<OrderKey> orderKeys) {
        return new LogicalSort<>(orderKeys, Optional.empty(),
                Optional.of(getLogicalProperties()), child(), false);
    }

    public LogicalSort<Plan> withOrderKeysPruned(boolean orderKeysPruned) {
        return new LogicalSort<>(orderKeys, groupExpression, Optional.of(getLogicalProperties()), child(),
                orderKeysPruned);
    }
}
