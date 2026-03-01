package name.abuchen.portfolio.ui.wizards.events;

import static name.abuchen.portfolio.ui.util.FormDataFactory.startingWith;
import static name.abuchen.portfolio.ui.util.SWTHelper.amountWidth;
import static name.abuchen.portfolio.ui.util.SWTHelper.widest;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityEvent.OptionDirection;
import name.abuchen.portfolio.model.SecurityEvent.OptionType;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.util.BindingHelper;
import name.abuchen.portfolio.ui.util.CurrencyToStringConverter;
import name.abuchen.portfolio.ui.util.DatePicker;
import name.abuchen.portfolio.ui.util.SecurityNameLabelProvider;
import name.abuchen.portfolio.ui.util.SimpleDateTimeDateSelectionProperty;
import name.abuchen.portfolio.ui.util.StringToCurrencyConverter;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.ui.wizards.AbstractWizardPage;

public class AddOptionEventPage extends AbstractWizardPage
{
    private IStylingEngine stylingEngine;
    private OptionEventModel model;
    private BindingHelper bindings;

    public AddOptionEventPage(IStylingEngine stylingEngine, OptionEventModel model)
    {
        super("add-option-event"); //$NON-NLS-1$

        setTitle(Messages.OptionEventWizardTitle);
        setDescription(Messages.OptionEventWizardDescription);

        this.stylingEngine = stylingEngine;
        this.model = model;

        bindings = new BindingHelper(model)
        {
            @Override
            public void onValidationStatusChanged(IStatus status)
            {
                boolean isOK = status.getSeverity() == IStatus.OK;
                setErrorMessage(isOK ? null : status.getMessage());
                setPageComplete(isOK);
            }
        };
    }

    @Override
    public void createControl(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NONE);
        setControl(container);
        container.setLayout(new FormLayout());

        // Security
        Label labelSecurity = new Label(container, SWT.RIGHT);
        labelSecurity.setText(Messages.ColumnSecurity);

        List<Security> securities = model.getClient().getActiveSecurities();
        if (model.getSecurity() != null && !securities.contains(model.getSecurity()))
            securities.add(0, model.getSecurity());

        ComboViewer comboSecurity = new ComboViewer(container, SWT.READ_ONLY);
        comboSecurity.setContentProvider(ArrayContentProvider.getInstance());
        comboSecurity.setLabelProvider(new SecurityNameLabelProvider(model.getClient()));
        comboSecurity.setInput(securities);

        // Trade date
        Label labelDate = new Label(container, SWT.NONE);
        labelDate.setText(Messages.ColumnDate);

        DatePicker boxDate = new DatePicker(container);

        // Option type: CALL / PUT
        Label labelOptionType = new Label(container, SWT.NONE);
        labelOptionType.setText(Messages.OptionEventLabelOptionType);

        ComboViewer comboOptionType = new ComboViewer(container, SWT.READ_ONLY | SWT.DROP_DOWN);
        comboOptionType.setContentProvider(ArrayContentProvider.getInstance());
        comboOptionType.setLabelProvider(new LabelProvider());
        comboOptionType.setInput(OptionType.values());

        // Direction: BOUGHT / SOLD
        Label labelDirection = new Label(container, SWT.NONE);
        labelDirection.setText(Messages.OptionEventLabelDirection);

        ComboViewer comboDirection = new ComboViewer(container, SWT.READ_ONLY | SWT.DROP_DOWN);
        comboDirection.setContentProvider(ArrayContentProvider.getInstance());
        comboDirection.setLabelProvider(new LabelProvider());
        comboDirection.setInput(OptionDirection.values());

        // Strike price
        Label labelStrike = new Label(container, SWT.NONE);
        labelStrike.setText(Messages.OptionEventLabelStrikePrice);

        Text txtStrike = new Text(container, SWT.BORDER | SWT.RIGHT);

        // Expiration date
        Label labelExpiration = new Label(container, SWT.NONE);
        labelExpiration.setText(Messages.OptionEventLabelExpirationDate);

        DatePicker boxExpiration = new DatePicker(container);

        // Premium
        Label labelPremium = new Label(container, SWT.NONE);
        labelPremium.setText(Messages.OptionEventLabelPremium);

        Text txtPremium = new Text(container, SWT.BORDER | SWT.RIGHT);

        // Contracts
        Label labelContracts = new Label(container, SWT.NONE);
        labelContracts.setText(Messages.OptionEventLabelContracts);

        Spinner spinnerContracts = new Spinner(container, SWT.BORDER);
        spinnerContracts.setMinimum(1);
        spinnerContracts.setMaximum(10000);
        spinnerContracts.setSelection(1);

        // form layout — measure after styling
        stylingEngine.style(container);

        int amountWidth = amountWidth(txtStrike);
        int labelWidth = widest(labelSecurity, labelDate, labelOptionType, labelDirection, labelStrike, labelExpiration,
                        labelPremium, labelContracts);

        startingWith(comboSecurity.getControl(), labelSecurity) //
                        .thenBelow(boxDate.getControl()).label(labelDate) //
                        .thenBelow(comboOptionType.getControl()).label(labelOptionType) //
                        .thenBelow(comboDirection.getControl()).label(labelDirection) //
                        .thenBelow(txtStrike).width(amountWidth).label(labelStrike) //
                        .thenBelow(boxExpiration.getControl()).label(labelExpiration) //
                        .thenBelow(txtPremium).width(amountWidth).label(labelPremium) //
                        .thenBelow(spinnerContracts).label(labelContracts);

        startingWith(labelSecurity).width(labelWidth);

        // model binding
        DataBindingContext context = bindings.getBindingContext();

        // security
        IObservableValue<?> targetSecurity = ViewerProperties.singleSelection().observe(comboSecurity);
        IObservableValue<?> modelSecurity = BeanProperties.value("security").observe(model); //$NON-NLS-1$
        context.bindValue(targetSecurity, modelSecurity, null, null);

        // trade date
        IObservableValue<LocalDate> targetDate = new SimpleDateTimeDateSelectionProperty()
                        .observe(boxDate.getControl());
        IObservableValue<LocalDate> modelDate = BeanProperties.value("date", LocalDate.class).observe(model); //$NON-NLS-1$
        context.bindValue(targetDate, modelDate, new UpdateValueStrategy<LocalDate, LocalDate>() //
                        .setAfterConvertValidator(value -> value != null ? ValidationStatus.ok()
                                        : ValidationStatus.error(MessageFormat.format(Messages.MsgDialogInputRequired,
                                                        Messages.ColumnDate))),
                        null);

        // option type
        IObservableValue<?> targetOptionType = ViewerProperties.singleSelection().observe(comboOptionType);
        IObservableValue<?> modelOptionType = BeanProperties.value("optionType").observe(model); //$NON-NLS-1$
        context.bindValue(targetOptionType, modelOptionType, null, null);

        // direction
        IObservableValue<?> targetDirection = ViewerProperties.singleSelection().observe(comboDirection);
        IObservableValue<?> modelDirection = BeanProperties.value("direction").observe(model); //$NON-NLS-1$
        context.bindValue(targetDirection, modelDirection, null, null);

        // strike price
        StringToCurrencyConverter strikeConverter = new StringToCurrencyConverter(Values.Amount);
        IObservableValue<String> targetStrike = WidgetProperties.text(SWT.Modify).observe(txtStrike);
        IObservableValue<Long> modelStrike = BeanProperties.value("strikePrice", Long.class).observe(model); //$NON-NLS-1$
        context.bindValue(targetStrike, modelStrike,
                        new UpdateValueStrategy<String, Long>().setAfterGetValidator(strikeConverter)
                                        .setConverter(strikeConverter)
                                        .setAfterConvertValidator(v -> v != null && v >= 0 ? ValidationStatus.ok()
                                                        : ValidationStatus.error(MessageFormat.format(
                                                                        Messages.MsgDialogInputRequired,
                                                                        Messages.OptionEventLabelStrikePrice))),
                        new UpdateValueStrategy<Long, String>()
                                        .setConverter(new CurrencyToStringConverter(Values.Amount)));

        // expiration date
        IObservableValue<LocalDate> targetExpiration = new SimpleDateTimeDateSelectionProperty()
                        .observe(boxExpiration.getControl());
        IObservableValue<LocalDate> modelExpiration = BeanProperties.value("expirationDate", LocalDate.class)
                        .observe(model);
        context.bindValue(targetExpiration, modelExpiration, new UpdateValueStrategy<LocalDate, LocalDate>() //
                        .setAfterConvertValidator(value -> value != null ? ValidationStatus.ok()
                                        : ValidationStatus.error(MessageFormat.format(Messages.MsgDialogInputRequired,
                                                        Messages.OptionEventLabelExpirationDate))),
                        null);

        // premium
        StringToCurrencyConverter premiumConverter = new StringToCurrencyConverter(Values.Amount);
        IObservableValue<String> targetPremium = WidgetProperties.text(SWT.Modify).observe(txtPremium);
        IObservableValue<Long> modelPremium = BeanProperties.value("premium", Long.class).observe(model); //$NON-NLS-1$
        context.bindValue(targetPremium, modelPremium,
                        new UpdateValueStrategy<String, Long>().setAfterGetValidator(premiumConverter)
                                        .setConverter(premiumConverter)
                                        .setAfterConvertValidator(v -> v != null && v >= 0 ? ValidationStatus.ok()
                                                        : ValidationStatus.error(MessageFormat.format(
                                                                        Messages.MsgDialogInputRequired,
                                                                        Messages.OptionEventLabelPremium))),
                        new UpdateValueStrategy<Long, String>()
                                        .setConverter(new CurrencyToStringConverter(Values.Amount)));

        // contracts (Spinner → int property)
        IObservableValue<Integer> targetContracts = WidgetProperties.spinnerSelection().observe(spinnerContracts);
        IObservableValue<Integer> modelContracts = BeanProperties.value("contracts", Integer.class).observe(model); //$NON-NLS-1$
        context.bindValue(targetContracts, modelContracts, null, null);
    }
}
