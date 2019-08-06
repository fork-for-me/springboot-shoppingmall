package com.shoppingmall.service;

import com.shoppingmall.domain.Cart;
import com.shoppingmall.domain.NormalUser;
import com.shoppingmall.domain.Product;
import com.shoppingmall.domain.ProductDisPrc;
import com.shoppingmall.dto.CartRequestDto;
import com.shoppingmall.dto.CartResponseDto;
import com.shoppingmall.dto.PagingDto;
import com.shoppingmall.exception.*;
import com.shoppingmall.repository.CartRepository;
import com.shoppingmall.repository.NormalUserRepository;
import com.shoppingmall.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class CartService {

    private NormalUserRepository normalUserRepository;
    private ProductRepository productRepository;
    private CartRepository cartRepository;

    public void makeCart(CartRequestDto cartRequestDto) {
        Optional<NormalUser> user = normalUserRepository.findById(cartRequestDto.getUserId());

        if (!user.isPresent())
            throw new NotExistUserException("존재하지 않는 유저입니다.");

        Optional<Product> productOpt = productRepository.findById(cartRequestDto.getProductId());

        if (!productOpt.isPresent())
            throw new NotExistProductException("존재하지 않는 상품입니다.");

        Product product = productOpt.get();

        if (product.getLimitCount() < cartRequestDto.getProductCount())
            throw new ProductLimitCountException("재고가 없습니다.");

        cartRepository.save(Cart.builder()
                .normalUser(user.get())
                .product(product)
                .productCount(cartRequestDto.getProductCount())
                .useYn('Y')
                .build());
    }

    public HashMap<String, Object> getCartList(Long userId, int page, Pageable pageable) {
        int realPage = page - 1;
        pageable = PageRequest.of(realPage, 5);

        Page<Cart> cartList = cartRepository.findAllByNormalUserIdAndUseYnOrderByCreatedDateDesc(userId, 'Y', pageable);

        if (cartList.getTotalElements() > 0) {
            List<CartResponseDto> cartResponseDtoList = new ArrayList<>();

            for (Cart cart : cartList) {
                int disPrice = 0;

                if (cart.getProduct().getProductDisPrcList().size() > 0) {
                    List<ProductDisPrc> disprcList
                            = cart.getProduct().getProductDisPrcList().stream().sorted().limit(1).collect(Collectors.toList());

                    disPrice = disprcList.get(0).getDisPrc();
                }

                cartResponseDtoList.add(cart.toResponseDto(disPrice));
            }

            PageImpl<CartResponseDto> cartLists = new PageImpl<>(cartResponseDtoList, pageable, cartList.getTotalElements());

            PagingDto cartPagingDto = new PagingDto();
            cartPagingDto.setPagingInfo(cartLists);

            List<Cart> carts = cartRepository.findAllByNormalUserIdAndUseYnOrderByCreatedDateDesc(userId, 'Y');
            int checkoutPrice = 0;
            List<Long> cartIdList = new ArrayList<>();

            for (Cart cart : carts) {
                cartIdList.add(cart.getId());

                int disPrice = 0;

                if (cart.getProduct().getProductDisPrcList().size() > 0) {
                    List<ProductDisPrc> disprcList
                            = cart.getProduct().getProductDisPrcList().stream().sorted().limit(1).collect(Collectors.toList());

                    disPrice = disprcList.get(0).getDisPrc();
                }

                int salePrice = (int)((((float) 100 - (float) disPrice) / (float)100) * cart.getProduct().getPrice());

                checkoutPrice += salePrice * cart.getProductCount();
            }

            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("cartList", cartLists);
            resultMap.put("cartPagingDto", cartPagingDto);
            resultMap.put("checkoutPrice", checkoutPrice);
            resultMap.put("cartIdList", cartIdList);

            return resultMap;
        }
        return null;
    }

    public void removeCart(Long id) {
        Optional<Cart> cartOpt = cartRepository.findById(id);

        if (!cartOpt.isPresent()) {
            throw new NotExistCartException("존재하지 않는 장바구니 입니다.");
        }

        cartRepository.delete(cartOpt.get());
    }

    public int checkReviewAuthority(HashMap<String, Object> paramMap) {
        Long userId    = Long.parseLong(paramMap.get("userId").toString());
        Long productId = Long.parseLong(paramMap.get("productId").toString());

        List<Cart> cartList = cartRepository.findAllByNormalUserIdAndProductId(userId, productId);

        if (cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (cart.getProductOrder() != null) {
                    return 1;
                }
            }
        }

        throw new CheckReviewAuthorityException("해당상품 결제를 완료한 회원만 리뷰를 작성할 수 있습니다.");
    }
}