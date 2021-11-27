package com.bannsi.peiceservice.controller;

import java.util.List;

import com.bannsi.peiceservice.DTO.PeiceRequest;
import com.bannsi.peiceservice.DTO.PeiceResponse;
import com.bannsi.peiceservice.DTO.ResponseDTO;
import com.bannsi.peiceservice.client.UserRestTemplateClient;
import com.bannsi.peiceservice.model.Peice;
import com.bannsi.peiceservice.model.User;
import com.bannsi.peiceservice.service.ImageService;
import com.bannsi.peiceservice.service.KeywordService;
import com.bannsi.peiceservice.service.PeiceService;
import com.bannsi.peiceservice.util.JwtUtil;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javassist.NotFoundException;

@RestController
@RequestMapping(value = "/peices/v1")
public class PeiceController {
    @Autowired
    private PeiceService peiceService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private UserRestTemplateClient userRestTemplateClient;
    
    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(PeiceController.class);

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getPeicesByUserId(@RequestHeader HttpHeaders headers){
        String token = headers.getFirst("Authorization").substring(7);
        String kakaoId = jwtUtil.getUsernameFromToken(token);
        logger.info(kakaoId);
        List<PeiceResponse> peiceResponses = peiceService.findPeiceByUserId(kakaoId);
        // List<PeiceResponse> peiceResponses = new ArrayList<>();
        // User user = userRestTemplateClient.getUser(kakaoId);
        // for(Peice peice : peices){
        //     peiceResponses.add(new PeiceResponse(peice, user, imageService.getImageUrl(peice.getPeiceId()), peice.getKeywords(), peice.getWhos()));
        // }
        return ResponseEntity.ok().body(new ResponseDTO("get peices by user id", peiceResponses));
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> savePeice(@RequestHeader HttpHeaders headers, @ModelAttribute PeiceRequest peiceRequest){
        logger.info("create peice");
        String token = headers.getFirst("Authorization").substring(7);
        logger.info(token);
        String kakaoId = jwtUtil.getUsernameFromToken(token);
        logger.info(kakaoId);
        peiceRequest.setTitle("tmp title");
        Peice peice = peiceService.savePeice(peiceRequest, kakaoId);
        logger.info(peice.getAddress());
        User user = userRestTemplateClient.getUser(kakaoId);
        return ResponseEntity.ok().body(new ResponseDTO("peice is saved", new PeiceResponse(peice, user, imageService.getImageUrl(peice.getPeiceId()), peice.getKeywords(), peice.getWhos())));
    }

    @RequestMapping(value="/{peiceId}/", method=RequestMethod.PUT)
    public ResponseEntity<?> updatePeice(@PathVariable Long peiceId, @RequestBody Peice peice) {
        try {
            peiceService.updatePeice(peiceId, peice);    
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(new ResponseDTO(e.getMessage(), null));
        }
        return ResponseEntity.ok().body(new ResponseDTO("peice updated", null));
    }
}
