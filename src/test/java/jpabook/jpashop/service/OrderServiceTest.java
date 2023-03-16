package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void orderItem() {
        //given
        Member member = createMember("회원1", "서울", "강가", "123-123");
        Book book = createBook("시골 JPA", 10000, 10);

        int resultTotalPrice = 20000;
        int resultStock = 8;
        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        assertEquals(1, getOrder.getOrderItems().size());
        assertEquals(resultTotalPrice, getOrder.getTotalPrice());
        assertEquals(resultStock, book.getStockQuantity());
    }

    @Test
    public void overStocked() {
        //given
        Member member = createMember("회원1", "서울", "강가", "123-123");
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //then
        assertThrows(NotEnoughStockException.class,
                () -> orderService.order(member.getId(), book.getId(), orderCount));
    }

    @Test
    public void orderCancel() {
        //given
        Member member = createMember("회원1", "서울", "강가", "123-123");
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals(10, book.getStockQuantity());
    }

    private Book createBook(String bookName, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(bookName);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String memberName, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(memberName);
        member.setAddress(new Address(city, street, zipcode));
        em.persist(member);
        return member;
    }

}