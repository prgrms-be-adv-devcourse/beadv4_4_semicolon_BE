package dukku.semicolon.boundedContext.order.in;

import dukku.semicolon.boundedContext.order.app.OrderFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private OrderFacade orderFacade;
}
