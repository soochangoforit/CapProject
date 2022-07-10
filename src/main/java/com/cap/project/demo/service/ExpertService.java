package com.cap.project.demo.service;

import com.cap.project.demo.domain.Attachment;
import com.cap.project.demo.domain.Department;
import com.cap.project.demo.domain.Expert;
import com.cap.project.demo.dto.request.ExpertJoinRequest;
import com.cap.project.demo.dto.response.ExpertResponse;
import com.cap.project.demo.repository.DepartmentRepository;
import com.cap.project.demo.repository.ExpertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExpertService {

    @Autowired
    private AttachmentServiceImpl attachmentServiceImpl;

    @Autowired
    private ExpertRepository expertRepository;

    // encoding expert password
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;


    @Transactional
    public Expert joinForExpert(ExpertJoinRequest expertJoinRequest) throws IOException {

        List<Attachment> attachments = attachmentServiceImpl.saveAttachments(expertJoinRequest.getAttachmentFiles());

        // get expertJoinRequest's password to do bcrypt encoding
        String password = expertJoinRequest.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        // 임의로 department repository를 통해서 데이터를 가져온다.
        Department department = getDepartment(expertJoinRequest.getDepartment_id());
        Expert expertEntity = expertJoinRequest.createExpertEntity(encodedPassword , department);

        attachments.stream().forEach(e->e.addExpert(expertEntity));


        return expertRepository.save(expertEntity);

    }

    private Department getDepartment(Long department_id) {

        if(departmentRepository.findById(department_id).isPresent()){
           return departmentRepository.findById(department_id).get();
        }
        return null;
    }

    public List<ExpertResponse> searchAllExperts() {

        List<Expert> experts = expertRepository.findAll();

        // each experts to ExpertResponse
        List<ExpertResponse> expertResponses = experts.stream()
                .map(e->ExpertResponse.builder()
                        .db_id(e.getId())
                        .name(e.getName())
                        .age(e.getAge())
                        .career(e.getCareer())
                        .hospital_name(e.getHospital_name())
                        .certificate_number(e.getCertificate_number())
                        .role(e.getRoleType())
                        .department(getDepartment(e.getDepartment().getId()))
                        .attachedFiles(e.getAttachedFiles())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return expertResponses;
    }

    public ExpertResponse getExpertInfo(Long db_id) {

        Expert expert = expertRepository.findById(db_id).orElse(null);

        if(expert != null){
            return ExpertResponse.builder()
                        .db_id(expert.getId())
                        .name(expert.getName())
                        .age(expert.getAge())
                        .career(expert.getCareer())
                        .certificate_number(expert.getCertificate_number())
                        .hospital_name(expert.getHospital_name())
                        .department(expert.getDepartment())
                        .attachedFiles(expert.getAttachedFiles())
                        .build();
        }

        return null;
    }
}
