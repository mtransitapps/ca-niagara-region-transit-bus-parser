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
// https://maps.niagararegion.ca/googletransit/NiagaraRegionTransit.zip
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
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
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

	private static final String NIAGARA_REGION_TRANSIT_AGENCY_ID = "NRT_F18_Niagara Region Transit";

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

	private static final Pattern STARTS_WITH_CT = Pattern.compile("(^(NRT \\- ))", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENDS_WITH_CT = Pattern.compile("( (nf|sc|we)$)", Pattern.CASE_INSENSITIVE);

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = STARTS_WITH_CT.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
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

	@Override
	public String getRouteColor(GRoute gRoute) {
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			int id = Integer.parseInt(matcher.group());
			switch (id) {
			// @formatter:off
			case 40: return "1B5C28";
			case 45: return "1B5C28";
			case 50: return "F1471C";
			case 55: return "F1471C";
			case 60: return "1378C7";
			case 65: return "1378C7";
			case 70: return "62B92C";
			case 75: return "62B92C";
			// @formatter:on
			}
		}
		System.out.printf("\nUnexpected route color for %s!\n", gRoute);
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

	@Override
	public String getStopCode(GStop gStop) {
		if (ZERO_0.equals(gStop.getStopCode())) {
			return null;
		}
		return super.getStopCode(gStop);
	}

	private static final String ZERO_0 = "0";
	private static final Pattern PRE_STOP_ID = Pattern.compile("(" //
			+ "NRT_[F|S|W][0-9]{2,4}_Stop|" //
			+ "NRT_[F|S|W][0-9]{2,4}Stop|" //
			+ "NRT_[F|S|W][0-9]{2,4}_|" //
			+ "NRT_[F|S|W][0-9]{2,4}|" //
			+ "NRT_[F|S|W]Stop|" //
			+ "NRT_[F|S|W]_Stop|" //
			//
			+ "NRT_[F|S|W]_[0-9]{2,4}_Stop|" //
			+ "NRT_[F|S|W]_[0-9]{2,4}_|" //
			//
			+ "NRT_[F|S|W]_|" //
			+ "NRT_[F|S|W]|" //
			//
			+ "[F|S|W][0-9]{4}_Stop|" //
			+ "[F|S|W][0-9]{4}_|" //
			//
			+ "Sto" //
			+ ")", //
			Pattern.CASE_INSENSITIVE);

	@Override
	public int getStopId(GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode == null || stopCode.length() == 0 || ZERO_0.equals(stopCode)) {
			stopCode = gStop.getStopId();
		}
		stopCode = PRE_STOP_ID.matcher(stopCode).replaceAll(StringUtils.EMPTY);
		if (Utils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		if (stopCode.equals("DTT")) {
			return 100_000;
		} else if (stopCode.equals("NFT")) {
			return 100_001;
		} else if (stopCode.equals("PEN")) {
			return 100_002;
		} else if (stopCode.equals("SWM")) {
			return 100_003;
		} else if (stopCode.equals("WEL")) {
			return 100004;
		} else if (stopCode.equals("BRU")) {
			return 100_006;
		} else if (stopCode.equals("FVM")) {
			return 100_008;
		} else if (stopCode.equals("GDC")) {
			return 100_020;
		} else if (stopCode.equals("WLC")) {
			return 100_021;
		} else if (stopCode.equals("CTO")) {
			return 100_035;
		} else if (stopCode.equals("OUT")) {
			return 100_044;
		}
		if (stopCode.equals("NiagSqua")) {
			return 200_000;
		} else if (stopCode.equals("Concentrix")) {
			return 200_001;
		}
		System.out.printf("\nUnexpected stop ID %s!\n", gStop);
		System.exit(-1);
		return -1;
	}
}
