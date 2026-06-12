package me.code.springboot_postgres.security;

import me.code.springboot_postgres.services.UserAccountService;
import me.code.springboot_postgres.services.RegistrationValidationService;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String API_PATH = "/api";
    private static final String ACCOUNT_PATH = API_PATH + "/account";
    private static final String PRODUCTS_PATH = API_PATH + "/products";
    private static final String ORDERS_PATH = API_PATH + "/orders";
    private static final String REVIEWS_PATH = API_PATH + "/reviews";

    private static final String[] PUBLIC_URLS = {
            ACCOUNT_PATH + "/register",
            ACCOUNT_PATH + "/login",
            PRODUCTS_PATH + "/all",
            PRODUCTS_PATH + "/featured",
            PRODUCTS_PATH + "/{productId}",
            PRODUCTS_PATH + "/search/**",
            PRODUCTS_PATH + "/category/**",
            PRODUCTS_PATH + "/categories",
            PRODUCTS_PATH + "/conditions",
            ORDERS_PATH + "/ongoing",
            ORDERS_PATH + "/delivery/methods",
            ORDERS_PATH + "/payment/methods",
            REVIEWS_PATH + "/product/**",
    };

    // SUPER_ADMIN only: role management
    private static final String[] SUPER_ADMIN_URLS = {
            API_PATH + "/admin/roles/**"
    };

    // ADMIN + SUPER_ADMIN: product & order management
    private static final String[] ADMIN_URLS = {
            API_PATH + "/admin_tools/**",
            API_PATH + "/admin/users/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security, UserAccountService userAccountService, JwtTokenUtil jwtTokenUtil) throws Exception {
        security.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(new JwtValidationFilter(jwtTokenUtil, userAccountService), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SUPER_ADMIN_URLS).hasAuthority("ROLE_READ")
                        .requestMatchers(ADMIN_URLS).hasAuthority("PRODUCT_READ_ALL")
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated());
        return security.build();
    }

    @Bean
    public AuthenticationProvider authProvider(UserDetailsService userAccountService, PasswordEncoder encoder) {
        var dao = new DaoAuthenticationProvider();

        dao.setUserDetailsService(userAccountService);
        dao.setPasswordEncoder(encoder);

        return dao;
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RegistrationValidationService registrationValidationService,
            me.code.springboot_postgres.repositories.RoleRepository roleRepository) {
        return new UserAccountService(userRepository, passwordEncoder, registrationValidationService, roleRepository);
    }

    @Bean
    public PasswordEncoder encoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
