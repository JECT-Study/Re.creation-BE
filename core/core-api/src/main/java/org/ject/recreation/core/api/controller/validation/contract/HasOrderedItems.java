package org.ject.recreation.core.api.controller.validation.contract;

import java.util.List;

public interface HasOrderedItems {
    List<? extends HasOrder> getOrderedItems();
}
