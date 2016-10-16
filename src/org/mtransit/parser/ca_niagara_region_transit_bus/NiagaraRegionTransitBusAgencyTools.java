package org.mtransit.parser.ca_niagara_region_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

// http://www.niagararegion.ca/government/opendata/data-set.aspx#id=32
// http://maps-dev.niagararegion.ca/GoogleTransit/NiagaraRegionTransit.zip
public class NiagaraRegionTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-niagara-region-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new NiagaraRegionTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Niagara Region Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Niagara Region Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	private static final String NIAGARA_REGION_TRANSIT_AGENCY_ID = "NR";

	private static final String EXCLUDE_STC_ROUTE_IDS_STARTS_WITH = "STC_";

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!NIAGARA_REGION_TRANSIT_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		if (gRoute.getRouteId().startsWith(EXCLUDE_STC_ROUTE_IDS_STARTS_WITH)) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String A = "A";

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(GRoute gRoute) {
		if (gRoute.getRouteShortName() != null && gRoute.getRouteShortName().length() > 0 && Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName());
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
		if (matcher.find()) {
			long id = Long.parseLong(matcher.group());
			if (gRoute.getRouteShortName().endsWith(A)) {
				return 10000l + id;
			}
		}
		System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
		System.exit(-1);
		return -1l;
	}

	private static final Pattern ENDS_WITH_CT = Pattern.compile("( (nf|sc|we)$)", Pattern.CASE_INSENSITIVE);

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = ENDS_WITH_CT.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
		routeLongName = CleanUtils.removePoints(routeLongName);
		return routeLongName;
	}

	private static final String AGENCY_COLOR_GREEN = "6CB33F"; // GREEN (from PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_F15A22 = "F15A22";
	private static final String COLOR_007AC2 = "007AC2";

	@Override
	public String getRouteColor(GRoute gRoute) {
		Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
		if (matcher.find()) {
			int id = Integer.parseInt(matcher.group());
			switch (id) {
			// @formatter:off
			case 40: return null; // agency color
			case 45: return null; // agency color
			case 50: return COLOR_F15A22;
			case 55: return COLOR_F15A22;
			case 60: return COLOR_007AC2;
			case 65: return COLOR_007AC2;
			case 70: return null; // agency color
			case 75: return null; // agency color
			// @formatter:on
			}
		}
		System.out.printf("\nUnexpected route long name for %s!\n", gRoute);
		System.exit(-1);
		return null;
	}


	private static final String NIAGARA_FALLS = "Niagara Falls";
	private static final String WELLAND = "Welland";
	private static final String ST_CATHARINES = "St Catharines";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		String tripHeadsign = gTrip.getTripHeadsign();
		Matcher matcher = DIGITS.matcher(mRoute.getShortName());
		if (matcher.find()) {
			int rsn = Integer.parseInt(matcher.group());
			switch (rsn) {
			// @formatter:off
			case 40: tripHeadsign = ST_CATHARINES; break;
			case 45: tripHeadsign = NIAGARA_FALLS; break;
			case 50: tripHeadsign = ST_CATHARINES; break;
			case 55: tripHeadsign = NIAGARA_FALLS; break;
			case 60: tripHeadsign = WELLAND; break;
			case 65: tripHeadsign = NIAGARA_FALLS; break;
			case 70: tripHeadsign = WELLAND; break;
			case 75: tripHeadsign = ST_CATHARINES; break;
			// @formatter:on
			default:
				System.out.printf("\nUnexpected trip %s!\n", gTrip);
				System.exit(-1);
			}
		}
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern AND = Pattern.compile("((^|\\W){1}(and)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AND_REPLACEMENT = "$2&$4";

	private static final Pattern AT = Pattern.compile("((^|\\W){1}(at)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = "$2/$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = AND.matcher(gStopName).replaceAll(AND_REPLACEMENT);
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final String NFWE = "NFWE";
	private static final String NF = "NF";
	private static final String WESC = "WESC";
	private static final String WE = "WE";
	private static final String NOTL = "NOTL";

	@Override
	public int getStopId(GStop gStop) {
		if (Utils.isDigitsOnly(gStop.getStopId())) {
			return Integer.parseInt(gStop.getStopId());
		}
		int indexOf;
		String stopIdS;
		indexOf = gStop.getStopId().indexOf(NFWE);
		if (indexOf >= 0) {
			stopIdS = gStop.getStopId().substring(indexOf + NFWE.length());
			if (Utils.isDigitsOnly(stopIdS)) {
				return 100000 + Integer.parseInt(stopIdS);
			}
		}
		indexOf = gStop.getStopId().indexOf(NF);
		if (indexOf >= 0) {
			stopIdS = gStop.getStopId().substring(indexOf + NF.length());
			if (Utils.isDigitsOnly(stopIdS)) {
				return 200000 + Integer.parseInt(stopIdS);
			}
		}
		indexOf = gStop.getStopId().indexOf(WESC);
		if (indexOf >= 0) {
			stopIdS = gStop.getStopId().substring(indexOf + WESC.length());
			if (Utils.isDigitsOnly(stopIdS)) {
				return 300000 + Integer.parseInt(stopIdS);
			}
		}
		indexOf = gStop.getStopId().indexOf(WE);
		if (indexOf >= 0) {
			stopIdS = gStop.getStopId().substring(indexOf + WE.length());
			if (Utils.isDigitsOnly(stopIdS)) {
				return 3400000 + Integer.parseInt(stopIdS);
			}
		}
		indexOf = gStop.getStopId().indexOf(NOTL);
		if (indexOf >= 0) {
			stopIdS = gStop.getStopId().substring(indexOf + NOTL.length());
			if (Utils.isDigitsOnly(stopIdS)) {
				return 500000 + Integer.parseInt(stopIdS);
			}
		}
		System.out.printf("\nUnexpected stop ID %s!\n", gStop);
		System.exit(-1);
		return -1;
	}
}
