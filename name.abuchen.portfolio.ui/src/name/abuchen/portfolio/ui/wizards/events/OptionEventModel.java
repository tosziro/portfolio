package name.abuchen.portfolio.ui.wizards.events;

import java.time.LocalDate;

import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityEvent.OptionDirection;
import name.abuchen.portfolio.model.SecurityEvent.OptionEvent;
import name.abuchen.portfolio.model.SecurityEvent.OptionType;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.ui.util.BindingHelper;

public class OptionEventModel extends BindingHelper.Model
{
    private Security security;
    private LocalDate date = LocalDate.now();
    private OptionType optionType = OptionType.CALL;
    private OptionDirection direction = OptionDirection.SOLD;
    private String currencyCode;
    private long strikePrice = 0;
    private LocalDate expirationDate = LocalDate.now().plusMonths(1);
    private long premium = 0;
    private int contracts = 1;

    public OptionEventModel(Client client, Security security)
    {
        super(client);
        this.security = security;
        this.currencyCode = security != null ? security.getCurrencyCode() : client.getBaseCurrency();
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        this.security = security;
        if (security != null)
            this.currencyCode = security.getCurrencyCode();
        firePropertyChange("security", this.security, this.security = security); //$NON-NLS-1$
    }

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        firePropertyChange("date", this.date, this.date = date); //$NON-NLS-1$
    }

    public OptionType getOptionType()
    {
        return optionType;
    }

    public void setOptionType(OptionType optionType)
    {
        firePropertyChange("optionType", this.optionType, this.optionType = optionType); //$NON-NLS-1$
    }

    public OptionDirection getDirection()
    {
        return direction;
    }

    public void setDirection(OptionDirection direction)
    {
        firePropertyChange("direction", this.direction, this.direction = direction); //$NON-NLS-1$
    }

    public String getCurrencyCode()
    {
        return currencyCode;
    }

    public long getStrikePrice()
    {
        return strikePrice;
    }

    public void setStrikePrice(long strikePrice)
    {
        firePropertyChange("strikePrice", this.strikePrice, this.strikePrice = strikePrice); //$NON-NLS-1$
    }

    public LocalDate getExpirationDate()
    {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate)
    {
        firePropertyChange("expirationDate", this.expirationDate, this.expirationDate = expirationDate); //$NON-NLS-1$
    }

    public long getPremium()
    {
        return premium;
    }

    public void setPremium(long premium)
    {
        firePropertyChange("premium", this.premium, this.premium = premium); //$NON-NLS-1$
    }

    public int getContracts()
    {
        return contracts;
    }

    public void setContracts(int contracts)
    {
        firePropertyChange("contracts", this.contracts, this.contracts = contracts); //$NON-NLS-1$
    }

    @Override
    public void applyChanges()
    {
        var strikeMoney = Money.of(currencyCode, strikePrice);
        var premiumMoney = Money.of(currencyCode, premium);
        var event = new OptionEvent(date, optionType, direction, strikeMoney, expirationDate, premiumMoney, contracts);
        security.addEvent(event);
    }
}
