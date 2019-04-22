/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.carbondata.mv.plans.util

import org.apache.spark.sql.catalyst.expressions.AttributeSet
import org.apache.spark.sql.catalyst.plans.logical.{Aggregate, LogicalPlan, Project}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.LogicalRelation

// This class add the columns when LogicalRelation is child of Aggregate
// this is ony for mv
object MVProjectColumnsAdd extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case a @ Aggregate(_, _, child) if (child.isInstanceOf[LogicalRelation] &&
      (child.outputSet -- a.references).isEmpty) =>
      a.copy(child = prunedChild(child, a.references))
  }


  /** Applies a projection only when the child is LogicalRelation */
  private def prunedChild(c: LogicalPlan, allReferences: AttributeSet) =
    if ((c.outputSet -- allReferences.filter(c.outputSet.contains)).isEmpty) {
      Project(c.output.filter(allReferences.contains), c)
    } else {
      c
    }
}
