package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SavePublisherDTO;
import com.example.demo.entity.Publisher;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.PublisherRepository;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.PublisherVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 出版社管理业务。 */
@Service
public class PublisherService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private BookRepository bookRepository;

    @Transactional(readOnly = true)
    public PageVo<PublisherVo> listPublishers(String keyword, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<Publisher> publishers = StringUtils.hasText(keyword)
                ? publisherRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                : publisherRepository.findAll(pageable);
        return PageVo.of(publishers.map(this::toVo));
    }

    @Transactional(readOnly = true)
    public PublisherVo getPublisher(Long publisherId) {
        return toVo(getPublisherOrThrow(publisherId));
    }

    @Transactional
    public PublisherVo createPublisher(SavePublisherDTO dto) {
        validateRequest(dto);
        String name = dto.getName().trim();
        if (publisherRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(HttpStatus.CONFLICT, "出版社名称已存在");
        }

        Publisher publisher = Publisher.builder()
                .name(name)
                .phone(trimToNull(dto.getPhone()))
                .address(trimToNull(dto.getAddress()))
                .introduction(trimToNull(dto.getIntroduction()))
                .build();
        return toVo(publisherRepository.save(publisher));
    }

    @Transactional
    public PublisherVo updatePublisher(Long publisherId, SavePublisherDTO dto) {
        validateRequest(dto);
        Publisher publisher = getPublisherOrThrow(publisherId);
        String name = dto.getName().trim();
        publisherRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(publisherId))
                .ifPresent(existing -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "出版社名称已存在");
                });

        publisher.setName(name);
        publisher.setPhone(trimToNull(dto.getPhone()));
        publisher.setAddress(trimToNull(dto.getAddress()));
        publisher.setIntroduction(trimToNull(dto.getIntroduction()));
        return toVo(publisherRepository.save(publisher));
    }

    @Transactional
    public void deletePublisher(Long publisherId) {
        Publisher publisher = getPublisherOrThrow(publisherId);
        if (bookRepository.existsByPublisher_Id(publisherId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "该出版社已关联图书，无法删除");
        }
        publisherRepository.delete(publisher);
    }

    private Publisher getPublisherOrThrow(Long publisherId) {
        if (publisherId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "出版社不能为空");
        }
        return publisherRepository.findById(publisherId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "出版社不存在"));
    }

    private void validateRequest(SavePublisherDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "出版社名称不能为空");
        }
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "页码必须从1开始，每页数量必须在1到" + MAX_PAGE_SIZE + "之间");
        }
        return PageRequest.of(page - 1, size,
                Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
    }

    private PublisherVo toVo(Publisher publisher) {
        PublisherVo vo = new PublisherVo();
        vo.setId(publisher.getId());
        vo.setName(publisher.getName());
        vo.setPhone(publisher.getPhone());
        vo.setAddress(publisher.getAddress());
        vo.setIntroduction(publisher.getIntroduction());
        vo.setCreateTime(publisher.getCreateTime());
        vo.setUpdateTime(publisher.getUpdateTime());
        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
