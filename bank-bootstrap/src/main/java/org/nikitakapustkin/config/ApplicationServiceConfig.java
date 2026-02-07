package org.nikitakapustkin.config;

import org.nikitakapustkin.application.ports.in.AddFriendUseCase;
import org.nikitakapustkin.application.ports.in.CreateAccountUseCase;
import org.nikitakapustkin.application.ports.in.CreateUserUseCase;
import org.nikitakapustkin.application.ports.in.DeleteUserUseCase;
import org.nikitakapustkin.application.ports.in.DepositMoneyUseCase;
import org.nikitakapustkin.application.ports.in.ImportUserUseCase;
import org.nikitakapustkin.application.ports.in.RemoveFriendUseCase;
import org.nikitakapustkin.application.ports.in.TransferMoneyUseCase;
import org.nikitakapustkin.application.ports.in.WithdrawMoneyUseCase;
import org.nikitakapustkin.application.ports.in.queries.GetAccountQuery;
import org.nikitakapustkin.application.ports.in.queries.GetAccountsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetTransactionsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetUserDetailsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetUserFriendsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetUsersQuery;
import org.nikitakapustkin.application.ports.out.CreateAccountPort;
import org.nikitakapustkin.application.ports.out.CreateUserPort;
import org.nikitakapustkin.application.ports.out.DeleteUserPort;
import org.nikitakapustkin.application.ports.out.LoadAccountPort;
import org.nikitakapustkin.application.ports.out.LoadAccountsPort;
import org.nikitakapustkin.application.ports.out.LoadFriendsPort;
import org.nikitakapustkin.application.ports.out.LoadTransactionsPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.LoadUsersPort;
import org.nikitakapustkin.application.ports.out.PublishAccountEventPort;
import org.nikitakapustkin.application.ports.out.PublishTransactionEventPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.application.ports.out.RecordTransactionPort;
import org.nikitakapustkin.application.ports.out.UpdateAccountStatePort;
import org.nikitakapustkin.application.ports.out.UpdateFriendsPort;
import org.nikitakapustkin.application.services.AddFriendService;
import org.nikitakapustkin.application.services.CreateAccountService;
import org.nikitakapustkin.application.services.CreateUserService;
import org.nikitakapustkin.application.services.DefaultCommissionPolicy;
import org.nikitakapustkin.application.services.DeleteUserService;
import org.nikitakapustkin.application.services.DepositMoneyService;
import org.nikitakapustkin.application.services.ImportUserService;
import org.nikitakapustkin.application.services.RemoveFriendService;
import org.nikitakapustkin.application.services.TransferMoneyService;
import org.nikitakapustkin.application.services.WithdrawMoneyService;
import org.nikitakapustkin.application.services.queries.GetAccountQueryService;
import org.nikitakapustkin.application.services.queries.GetAccountsQueryService;
import org.nikitakapustkin.application.services.queries.GetTransactionsQueryService;
import org.nikitakapustkin.application.services.queries.GetUserDetailsQueryService;
import org.nikitakapustkin.application.services.queries.GetUserFriendsQueryService;
import org.nikitakapustkin.application.services.queries.GetUsersQueryService;
import org.nikitakapustkin.domain.services.CommissionPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class ApplicationServiceConfig {

    @Bean
    public CommissionPolicy commissionPolicy(
            @Value("${transfer.commission.friends:0.03}") BigDecimal friendsRate,
            @Value("${transfer.commission.others:0.10}") BigDecimal othersRate
    ) {
        return new DefaultCommissionPolicy(friendsRate, othersRate);
    }

    @Bean
    public AddFriendUseCase addFriendUseCase(
            LoadUserPort loadUserPort,
            UpdateFriendsPort updateFriendsPort,
            PublishUserEventPort publishUserEventPort
    ) {
        return new AddFriendService(loadUserPort, updateFriendsPort, publishUserEventPort);
    }

    @Bean
    public RemoveFriendUseCase removeFriendUseCase(
            LoadUserPort loadUserPort,
            UpdateFriendsPort updateFriendsPort,
            PublishUserEventPort publishUserEventPort
    ) {
        return new RemoveFriendService(loadUserPort, updateFriendsPort, publishUserEventPort);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(
            LoadUserPort loadUserPort,
            CreateUserPort createUserPort,
            PublishUserEventPort publishUserEventPort
    ) {
        return new CreateUserService(loadUserPort, createUserPort, publishUserEventPort);
    }

    @Bean
    public ImportUserUseCase importUserUseCase(
            LoadUserPort loadUserPort,
            CreateUserPort createUserPort
    ) {
        return new ImportUserService(loadUserPort, createUserPort);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(DeleteUserPort deleteUserPort) {
        return new DeleteUserService(deleteUserPort);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(
            LoadUserPort loadUserPort,
            CreateAccountPort createAccountPort,
            PublishAccountEventPort publishAccountEventPort
    ) {
        return new CreateAccountService(loadUserPort, createAccountPort, publishAccountEventPort);
    }

    @Bean
    public DepositMoneyUseCase depositMoneyUseCase(
            LoadAccountPort loadAccountPort,
            UpdateAccountStatePort updateAccountStatePort,
            RecordTransactionPort recordTransactionPort,
            PublishAccountEventPort publishAccountEventPort,
            PublishTransactionEventPort publishTransactionEventPort
    ) {
        return new DepositMoneyService(
                loadAccountPort,
                updateAccountStatePort,
                recordTransactionPort,
                publishAccountEventPort,
                publishTransactionEventPort
        );
    }

    @Bean
    public WithdrawMoneyUseCase withdrawMoneyUseCase(
            LoadAccountPort loadAccountPort,
            UpdateAccountStatePort updateAccountStatePort,
            RecordTransactionPort recordTransactionPort,
            PublishAccountEventPort publishAccountEventPort,
            PublishTransactionEventPort publishTransactionEventPort
    ) {
        return new WithdrawMoneyService(
                loadAccountPort,
                updateAccountStatePort,
                recordTransactionPort,
                publishAccountEventPort,
                publishTransactionEventPort
        );
    }

    @Bean
    public TransferMoneyUseCase transferMoneyUseCase(
            LoadAccountPort loadAccountPort,
            UpdateAccountStatePort updateAccountStatePort,
            LoadFriendsPort loadFriendsPort,
            RecordTransactionPort recordTransactionPort,
            PublishAccountEventPort publishAccountEventPort,
            PublishTransactionEventPort publishTransactionEventPort,
            CommissionPolicy commissionPolicy
    ) {
        return new TransferMoneyService(
                loadAccountPort,
                updateAccountStatePort,
                loadFriendsPort,
                recordTransactionPort,
                publishAccountEventPort,
                publishTransactionEventPort,
                commissionPolicy
        );
    }

    @Bean
    public GetUsersQuery getUsersQuery(LoadUsersPort loadUsersPort) {
        return new GetUsersQueryService(loadUsersPort);
    }

    @Bean
    public GetUserDetailsQuery getUserDetailsQuery(
            LoadUserPort loadUserPort,
            LoadFriendsPort loadFriendsPort,
            LoadAccountsPort loadAccountsPort
    ) {
        return new GetUserDetailsQueryService(loadUserPort, loadFriendsPort, loadAccountsPort);
    }

    @Bean
    public GetUserFriendsQuery getUserFriendsQuery(
            LoadUserPort loadUserPort,
            LoadFriendsPort loadFriendsPort
    ) {
        return new GetUserFriendsQueryService(loadUserPort, loadFriendsPort);
    }

    @Bean
    public GetAccountsQuery getAccountsQuery(LoadAccountsPort loadAccountsPort) {
        return new GetAccountsQueryService(loadAccountsPort);
    }

    @Bean
    public GetAccountQuery getAccountQuery(LoadAccountPort loadAccountPort) {
        return new GetAccountQueryService(loadAccountPort);
    }

    @Bean
    public GetTransactionsQuery getTransactionsQuery(LoadTransactionsPort loadTransactionsPort) {
        return new GetTransactionsQueryService(loadTransactionsPort);
    }
}
