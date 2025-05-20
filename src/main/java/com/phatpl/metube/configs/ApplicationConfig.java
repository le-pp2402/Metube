package com.phatpl.metube.configs;

import com.phatpl.metube.models.User;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.video.RabbitMQUploadService;
import com.phatpl.metube.utils.BCryptPassword;
import com.phatpl.metube.utils.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:3000/")
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;
    private final RabbitMQUploadService rabbitMQUploadService;

    // Lấy người người từ database lên bằng username để provider xác thực
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) throw new UsernameNotFoundException("not found " + username);
            return new CustomUserDetail(user.get());
        };
    }

    // Cung cấp nhiều cách xác thực khác nhau chằng hạn như DAO, OAuth2Login, LDAP, ...
    // Ở đây mình chọn bộ xác thực là Dao và set userDetailsService cho nó là userDetailsService() ở trên
    // Ngoài ra mình set passwordEncoder là BCryptPasswordEncoder với độ dài salt là 8 để mã hóa mật khẩu măc định
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder(8));
        return authProvider;
    }

    // Tạo ra một bean AuthenticationManager để sử dụng trong việc xác thực giao tiếp với các lớp filter
    // và các lớp xác thực khác
    // cụ thể nó sẽ duyệt qua các providers và tìm kiếm provider phù hợp để xác thực
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            if (userRepository.findByUsername("admin123").isEmpty()) {
                User user = new User();
                user.setUsername("admin123");
                user.setPassword(BCryptPassword.encode("Admin@123"));
                user.setIsAdmin(true);
                user.setCode(0);
                user.setActivated(true);
                user.setEmail("admin123@gmail.com");
                userRepository.save(user);
            }

            rabbitMQUploadService.run();
        };
    }

}
