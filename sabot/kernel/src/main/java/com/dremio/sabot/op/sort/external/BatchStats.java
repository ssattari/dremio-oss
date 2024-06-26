/*
 * Copyright (C) 2017-2019 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.sabot.op.sort.external;

import com.dremio.exec.record.VectorAccessible;
import com.dremio.exec.record.VectorWrapper;
import java.util.IdentityHashMap;
import java.util.Map;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.memory.ReferenceManager;
import org.apache.arrow.vector.ValueVector;

class BatchStats {

  enum SizeType {
    ACCOUNTED,
    WORSE_CASE
  }

  private Map<ReferenceManager, Void> accountedBuffers = new IdentityHashMap<>();

  public long getSize(VectorAccessible va, SizeType type) {
    long size = 0;
    for (VectorWrapper<?> wrapper : va) {
      if (wrapper.isHyper()) {
        for (ValueVector v : wrapper.getValueVectors()) {
          size += getSize(v, type);
        }
      } else {
        size += getSize(wrapper.getValueVector(), type);
      }
    }
    return size;
  }

  public long getSize(ValueVector v, SizeType type) {
    long size = 0;
    ArrowBuf[] buffers = v.getBuffers(false);
    for (ArrowBuf b : buffers) {
      switch (type) {
        case ACCOUNTED:
          size += b.getActualMemoryConsumed();
          break;
        case WORSE_CASE:
          ReferenceManager referenceManager = b.getReferenceManager();
          // avoid counting the same underlying buffer multiple times
          if (accountedBuffers.containsKey(referenceManager)) {
            continue;
          }
          size += b.getPossibleMemoryConsumed();
          accountedBuffers.put(referenceManager, null);
          break;
        default:
          throw new UnsupportedOperationException("Invalid case: " + type.name());
      }
    }
    return size;
  }
}
