//package com.example.store.service;
//
//import com.example.store.model.Admin;
//import com.example.store.repository.AdminRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class AdminService {
//
//    @Autowired
//    private AdminRepository adminRepository;
//
//    // Create a new Admin
//    public Admin createAdmin(Admin admin) {
//        return adminRepository.save(admin);
//    }
//
//    // Get all Admins
//    public List<Admin> getAllAdmins() {
//        return adminRepository.findAll();
//    }
//
//    // Get Admin by ID
//    public Optional<Admin> getAdminById(UUID id) {
//        return adminRepository.findById(id);
//    }
//}
