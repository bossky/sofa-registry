/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.CommonResponse;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 11:05 yuzhi.lyz Exp $
 */
public final class SlotAccessGenericResponse<T> extends CommonResponse {
    private final SlotAccess slotAccess;
    private       T          data;

    private SlotAccessGenericResponse(boolean success, String message, SlotAccess slotAccess) {
        super(success, message);
        this.slotAccess = slotAccess;
    }

    /**
     * Getter method for property <tt>slotAccess</tt>.
     * @return property value of slotAccess
     */
    public SlotAccess getSlotAccess() {
        return slotAccess;
    }

    public static <T> SlotAccessGenericResponse<T> buildSuccessResponse(SlotAccess access) {
        return new SlotAccessGenericResponse(true, null, access);
    }

    public static <T> SlotAccessGenericResponse<T> buildFailedResponse(SlotAccess access) {
        return new SlotAccessGenericResponse(false, access.toString(), access);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
