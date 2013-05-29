package me.desht.dhutils.midi;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Utility for playing midi files for players to hear.
 *
 * @author authorblues
 */
public class MidiUtil
{
	private static Sequencer sequencer;

	public static void playMidi(File file, float tempo, Set<Player> listeners)
		throws InvalidMidiDataException, IOException, MidiUnavailableException
	{
		play(file, tempo, new NoteBlockReceiver(listeners));
	}

	public static void playMidi(File file, float tempo, Location loc) 
			throws InvalidMidiDataException, IOException, MidiUnavailableException
	{
		play(file, tempo, new NoteBlockReceiver(loc));
	}

	public static boolean playMidiQuietly(File file, float tempo, Set<Player> listeners)
	{
		try { MidiUtil.playMidi(file, tempo, listeners); }
		catch (MidiUnavailableException e) { e.printStackTrace(); return false; }
		catch (InvalidMidiDataException e) { e.printStackTrace(); return false; }
		catch (IOException e) { e.printStackTrace(); return false; }

		return true;
	}

	public static boolean playMidiQuietly(File file, float tempo, Location loc)
	{
		try { MidiUtil.playMidi(file, tempo, loc); }
		catch (MidiUnavailableException e) { e.printStackTrace(); return false; }
		catch (InvalidMidiDataException e) { e.printStackTrace(); return false; }
		catch (IOException e) { e.printStackTrace(); return false; }

		return true;
	}

	public static boolean playMidiQuietly(File file, Set<Player> listeners)
	{
		return playMidiQuietly(file, 1.0f, listeners);
	}

	private static void play(File file, float tempo, NoteBlockReceiver recv) 
			throws InvalidMidiDataException, IOException, MidiUnavailableException
	{
		if (sequencer == null) {
			sequencer = MidiSystem.getSequencer(false);
			sequencer.addMetaEventListener(new MetaEventListener() {
	            @Override
	            public void meta(MetaMessage metaMsg) {
	                if (metaMsg.getType() == 0x2F) {
	                    sequencer.close();
	                }
	            }
	        });
		} else if (sequencer.isOpen()) {
			sequencer.close(); // in case any other sequence is already playing
		}
		sequencer.setSequence(MidiSystem.getSequence(file));
		sequencer.open();

		// slow it down just a bit
		sequencer.setTempoFactor(tempo);

		sequencer.getTransmitter().setReceiver(recv);
		sequencer.start();
	}
}
