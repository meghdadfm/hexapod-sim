package net.isammoc.hexapod;

import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class HexapodControlPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final HexapodConverter model;
	private final Map<HexapodLeg, Map<HexapodArticulation, SpinnerNumberModel>> spinModels;
	{
		this.spinModels = new EnumMap<HexapodLeg, Map<HexapodArticulation, SpinnerNumberModel>>(
				HexapodLeg.class);
		for (final HexapodLeg leg : HexapodLeg.values()) {
			this.spinModels.put(leg, new EnumMap<HexapodArticulation, SpinnerNumberModel>(
					HexapodArticulation.class));
		}
	}

	public HexapodControlPanel(final HexapodConverter model) {
		this.model = model;
		this.setLayout(new GridLayout(0, 4));

		this.add(new JLabel());
		this.add(new JLabel("A3"));
		this.add(new JLabel("A2"));
		this.add(new JLabel("A1"));

		this.createLeg("P1", HexapodLeg.RIGHT_FRONT);
		this.createLeg("P2", HexapodLeg.RIGHT_MIDDLE);
		this.createLeg("P3", HexapodLeg.RIGHT_REAR);
		this.createLeg("P4", HexapodLeg.LEFT_REAR);
		this.createLeg("P5", HexapodLeg.LEFT_MIDDLE);
		this.createLeg("P6", HexapodLeg.LEFT_FRONT);
	}

	private void createLeg(final String name, final HexapodLeg leg) {
		this.add(new JLabel(name));
		SpinnerNumberModel spinModel;
		spinModel = new SpinnerNumberModel(1, 1, 254, 1);
		spinModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				HexapodControlPanel.this.model.setValue(leg, HexapodArticulation.SHOULDER,
						(Integer) ((SpinnerNumberModel) e.getSource()).getNumber());
			}
		});
		this.add(new JSpinner(spinModel));
		this.spinModels.get(leg).put(HexapodArticulation.SHOULDER, spinModel);

		spinModel = new SpinnerNumberModel(1, 1, 254, 1);
		spinModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				HexapodControlPanel.this.model.setValue(leg, HexapodArticulation.ELBOW,
						(Integer) ((SpinnerNumberModel) e.getSource()).getNumber());
			}
		});
		this.add(new JSpinner(spinModel));
		this.spinModels.get(leg).put(HexapodArticulation.ELBOW, spinModel);

		spinModel = new SpinnerNumberModel(1, 1, 254, 1);
		spinModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				HexapodControlPanel.this.model.setValue(leg, HexapodArticulation.WRIST,
						(Integer) ((SpinnerNumberModel) e.getSource()).getNumber());
			}
		});
		this.add(new JSpinner(spinModel));
		this.spinModels.get(leg).put(HexapodArticulation.WRIST, spinModel);
	}

	public Map<HexapodLeg, Map<HexapodArticulation, SpinnerNumberModel>> getSpinModels() {
		return this.spinModels;
	}
}
