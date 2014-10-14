package net.za.acraig.reformatter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class ReformatterActivity extends Activity 
{
	private String _diary = "nothing to show";
	private String _desc = "<em>Italics description...</em>\n\n";
	private String _title = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent i = getIntent();
		//if (Intent.ACTION_SEND.equals(i))
			{
			Bundle extras = i.getExtras();
			if (extras != null)
				{
				String title = extras.getString("android.intent.extra.SUBJECT");
				_diary = title.replaceAll("MomentDiary: ", "The week of ");
				_diary += "\n\n";
				
				_title = titleFromDates(title);
				_diary += _desc;
				_diary += extras.getString("android.intent.extra.TEXT");
				}
			}

		if (0 != _diary.compareTo("nothing to show"))
			{
			String podcasts = extractPodcasts();
			stripPodcastEntries();
			reformatEntries();
			_diary += podcasts;
			}
		
		TextView text = (TextView) findViewById(R.id.text1);
		if (text != null)
			{
			text.setText(_diary + "\n");
			text.setMovementMethod(new ScrollingMovementMethod());
			}
		}
    
    public void onResendClick(View view)
    	{
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, _title);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, _diary);
		startActivity(intent);
    	}

    private String extractPodcasts()
    	{
    	String url = "http://acraig.za.net/podcasts.html";
    	//String podcasts = String.format("<em><a href=\"%s\">Podcasts</a> listened to this week: </em>", url);
    	String podcasts = url + "<em>Podcasts listened to this week:</em> ";

    	Pattern p = Pattern.compile("(\\S+).mp3\n");
    	Matcher m = p.matcher(_diary);
    	while (m.find())
    		{
    		String name = expandPodcastName(m.group(1));
    		podcasts += name;
    		podcasts += "; ";
    		}

    	return podcasts.trim();
    	}

    private void stripPodcastEntries()
    	{
    	String date = "- [0-9]{4}/[0-9]{2}/[0-9]{2} ";
    	String day = ".+\\S";
    	String time = " [0-9]{2}:[0-9]{2}";
    	String title = "\nPodcasts listened to this week:\n";
    	String files = "(.+.mp3\n)+";
    	String regex = "\n" + date + day + time + title + files;
    	
    	_diary = _diary.replaceAll(regex, "");
    	}

    private void reformatEntries()
    	{
    	String date = "- [0-9]{4}/[0-9]{2}/[0-9]{2} ";
    	String time = " [0-9]{2}:[0-9]{2}\n";
    	
    	String day0 = date + "Monday" + time;
    	String day1 = date + "Tuesday" + time;
    	String day2 = date + "Wednesday" + time;
    	String day3 = date + "Thursday" + time;
    	String day4 = date + "Friday" + time;
    	String day5 = date + "Saturday" + time;
    	String day6 = date + "Sunday" + time;

    	_diary = _diary.replaceAll(day0, "<b>Monday:</b> ");
    	_diary = _diary.replaceAll(day1, "<b>Tuesday:</b> ");
    	_diary = _diary.replaceAll(day2, "<b>Wednesday:</b> ");
    	_diary = _diary.replaceAll(day3, "<b>Thursday:</b> ");
    	_diary = _diary.replaceAll(day4, "<b>Friday:</b> ");
    	_diary = _diary.replaceAll(day5, "<b>Saturday:</b> ");
    	_diary = _diary.replaceAll(day6, "<b>Sunday:</b> ");

    	_diary = _diary.replaceAll("\n\n\n", "\n\n");
    	}

    private String expandPodcastName(String filename)
    	{
    	// strip off date prefix
    	String ret = filename;
    	Pattern p = Pattern.compile("([0-9]{8})_.+");
    	Matcher m = p.matcher(ret);
    	if (m.find())
    		ret = ret.substring(9);
    	
    	ret = ret.replace("aaa", "All About Android ");
    	ret = ret.replace("twit", "This Week in Tech ");
    	ret = ret.replace("zats", "ZA Tech Show ");
    	ret = ret.replace("LetsTalkGeek_E", "Let's Talk Geek ");
    	ret = ret.replace("floss", "FLOSS Weekly ");
    	ret = ret.replace("hanselminutes_", "Hanselminutes ");
    	ret = ret.replace("WT_", "Wood Talk ");
    	ret = ret.replace("wt_", "Wood Talk ");
    	ret = ret.replace("inbeta-", "InBeta ");
    	ret = ret.replace("hpr", "Hacker Public Radio ");
    	
    	p = Pattern.compile("([0-9]{2})-([A-Z][a-z]+)201([0-9]{1})");
    	m = p.matcher(ret);
    	if (m.find())
    		ret = "Pit Pass " + m.group(1);
    	
    	p = Pattern.compile("([0-9]{2,3}[a-z]?)-[A-Z].+");
    	m = p.matcher(ret);
    	if (m.find())
    		ret = "This Developer's Life " + m.group(1);
    	
    	p = Pattern.compile("([0-9]{8,9})-stack-exchange-stack-exchange-podcast-episode-([0-9]{2,2}).+");
    	m = p.matcher(ret);
    	if (m.find())
    		ret = "Stack Exchange " + m.group(2);
    	
    	p = Pattern.compile("seradio-episode([0-9]{3})-.+");
    	m = p.matcher(ret);
    	if (m.find())
    		ret = "Software Engineering Radio " + m.group(1);
    	
    	p = Pattern.compile("Episode([0-9]{2})");
    	m = p.matcher(ret);
    	if (m.find())
    		ret = "Hello World " + m.group(1);
    	
    	return ret;
    	}
    
    private String titleFromDates(String dates)
    	{
 		Calendar start = new GregorianCalendar(2011, 8, 15);
		Calendar end = new GregorianCalendar();
		
		Pattern p = Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2}");
    	Matcher m = p.matcher(dates);
    	if (m.find())
    		{
    		String tokens[] = m.group().split("/");
    		if (tokens.length >= 3)
    			end.set(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
    		}
    	
		int yearInterval = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
		int startWeeks = start.get(Calendar.WEEK_OF_YEAR);
		int endWeeks = end.get(Calendar.WEEK_OF_YEAR);
		endWeeks += 52 * yearInterval;
		int weekInterval = endWeeks - startWeeks + 1;
		
		String title = String.format(Locale.getDefault(), "Week %d", weekInterval);
		return title;
		}
    }
