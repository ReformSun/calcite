/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.sql.fun;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlOperandTypeChecker;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlValidator;

/**
 * The JSON API common syntax including a path specification, which is for
 * JSON querying and processing.
 */
public class SqlJsonApiCommonSyntaxOperator extends SqlSpecialOperator {

  // If true, the syntax must contain a JSON path expression, e.g. '{'foo':'bar'}', 'lax $.foo';
  // otherwise the syntax can be specified within JSON text only, e.g. '{'foo':'bar'}'.
  private final boolean hasPath;

  public SqlJsonApiCommonSyntaxOperator(String name, boolean hasPath) {
    super(name, SqlKind.JSON_API_COMMON_SYNTAX, 100, true,
        ReturnTypes.explicit(SqlTypeName.ANY), null,
        hasPath ? OperandTypes.family(SqlTypeFamily.ANY, SqlTypeFamily.STRING)
            : OperandTypes.family(SqlTypeFamily.ANY));
    this.hasPath = hasPath;
  }

  @Override protected void checkOperandCount(SqlValidator validator,
      SqlOperandTypeChecker argType, SqlCall call) {
    if (hasPath) {
      if (call.operandCount() < 2) {
        throw new UnsupportedOperationException(
            "JSON API common syntax requires at least 2 parameters");
      }
    } else {
      if (call.operandCount() < 1) {
        throw new UnsupportedOperationException(
            "JSON API common syntax requires at least 1 parameter");
      }
    }
  }

  @Override public void unparse(SqlWriter writer, SqlCall call, int leftPrec,
      int rightPrec) {
    SqlWriter.Frame frame = writer.startList(SqlWriter.FrameTypeEnum.SIMPLE);
    call.operand(0).unparse(writer, 0, 0);
    if (hasPath) {
      writer.sep(",", true);
      call.operand(1).unparse(writer, 0, 0);
    }
    if (hasPath && call.operandCount() > 2
        || !hasPath && call.operandCount() > 1) {
      writer.keyword("PASSING");
      for (int i = 2; i < call.getOperandList().size(); i += 2) {
        call.operand(i).unparse(writer, 0, 0);
        writer.keyword("AS");
        call.operand(i + 1).unparse(writer, 0, 0);
      }
    }
    writer.endFunCall(frame);
  }
}

// End SqlJsonApiCommonSyntaxOperator.java
