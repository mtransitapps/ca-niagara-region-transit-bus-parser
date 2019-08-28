package org.mtransit.parser.ca_niagara_region_transit_bus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

	private static final String NIAGARA_REGION_TRANSIT = "Niagara Region Transit";

	private static final String EXCLUDE_STC_ROUTE_IDS_STARTS_WITH = "STC_";

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!gRoute.getAgencyId().contains(NIAGARA_REGION_TRANSIT)) {
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
				return 10_000L + id;
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
			case 22: return "766A24";
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

	private static final String SPACE = " ";

	private static final String A_ = "A ";

	private static final String NIAGARA = "Niagara";
	private static final String NIAGARA_COLLEGE = NIAGARA + " College";
	private static final String NIAGARA_FALLS = NIAGARA + " Falls";
	private static final String NOTL_CAMPUS = "NOTL Campus";
	private static final String OUTLET_MALL = "Outlet Mall";
	private static final String ST_CATHARINES = "St Catharines";
	private static final String WELLAND = "Welland";
	private static final String WELLAND_CAMPUS = WELLAND + " Campus";

	public static final Pattern STARTS_WITH_NRT_A00_ = Pattern.compile( //
			"((^){1}(nrt\\_[A-Z]{1}[\\d]{2}(\\_)?([A-Z]{3}(stop))?(stop)?)(NFT)?)", //
			Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopOriginalId(String gStopId) {
		gStopId = STARTS_WITH_NRT_A00_.matcher(gStopId).replaceAll(StringUtils.EMPTY);
		return gStopId;
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		String tripHeadsign = gTrip.getTripHeadsign();
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("((^){1}[\\d]{2}([A-Z]{1} | ))", Pattern.CASE_INSENSITIVE);
	private static final String STARTS_WITH_RSN_REPLACEMENT = "$3";

	private static final Pattern IMT_ = Pattern.compile("((^|\\W){1}(imt \\-|imt)(\\W|$){1})", Pattern.CASE_INSENSITIVE); // Inter-Municipal Transit

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(([A-Z]{1} )?(.*)( to | \\- |\\- |\\-)(.*))", Pattern.CASE_INSENSITIVE);
	private static final String STARTS_WITH_TO_REPLACEMENT = "$2$5";

	private static final Pattern ST_CATHARINES_ = Pattern.compile("((^|\\W){1}(" //
			+ "st\\. catharines" + "|" //
			+ "st\\. catharine" + "|" //
			+ "st\\. catharin" + "|" //
			+ "st\\. cathari" + "|" //
			+ "st\\. cathar" + "|" //
			+ "st\\. catha" + "|" //
			+ "st\\. cath" + "|" //
			+ "st\\. cat" + "|" //
			+ "st\\. ca" + "|" //
			+ "st\\. c" + "|" //
			+ "st\\. " //
			+ ")(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String ST_CATHARINES_REPLACEMENT = "$2" + ST_CATHARINES + "$4";

	private static final Pattern NIAGARA_FALLS_ = Pattern.compile("((^|\\W){1}(" //
			+ "niagara falls" + "|" //
			+ "niagara fall" + "|" //
			+ "niag" + "|" //
			+ "nia\\. falls" + "|" //
			+ "falls" //
			+ ")(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String NIAGARA_FALLS_REPLACEMENT = "$2" + NIAGARA_FALLS + "$4";

	private static final Pattern OUTLET_MALL_ = Pattern.compile("((^|\\W){1}(" //
			+ "outlet mall" + "|" //
			+ "outlet m" //
			+ ")(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String OUTLET_MALL_REPLACEMENT = "$2" + OUTLET_MALL + "$4";

	private static final Pattern WELLAND_ = Pattern.compile("((^|\\W){1}(" //
			+ "welland" + "|" //
			+ "wellan" //
			+ ")(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String WELLAND_REPLACEMENT = "$2" + WELLAND + "$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(STARTS_WITH_RSN_REPLACEMENT);
		tripHeadsign = IMT_.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(STARTS_WITH_TO_REPLACEMENT);
		tripHeadsign = ST_CATHARINES_.matcher(tripHeadsign).replaceAll(ST_CATHARINES_REPLACEMENT);
		tripHeadsign = NIAGARA_FALLS_.matcher(tripHeadsign).replaceAll(NIAGARA_FALLS_REPLACEMENT);
		tripHeadsign = OUTLET_MALL_.matcher(tripHeadsign).replaceAll(OUTLET_MALL_REPLACEMENT);
		tripHeadsign = WELLAND_.matcher(tripHeadsign).replaceAll(WELLAND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 40L) {
			if (Arrays.asList( //
					A_ + NIAGARA_COLLEGE + SPACE + NOTL_CAMPUS, //
					ST_CATHARINES //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ST_CATHARINES, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 45L) {
			if (Arrays.asList( //
					A_ + NIAGARA_FALLS, //
					NIAGARA_FALLS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NIAGARA_FALLS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 60L) {
			if (Arrays.asList( //
					A_ + WELLAND_CAMPUS, //
					WELLAND //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WELLAND, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 65L) {
			if (Arrays.asList( //
					A_ + NIAGARA_FALLS, //
					NIAGARA_FALLS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NIAGARA_FALLS, mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexptected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	@Override
	public String cleanStopName(String gStopName) {
		if (Utils.isUppercaseOnly(gStopName, true, true)) {
			gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		}
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
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

	@Override
	public int getStopId(GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode == null || stopCode.length() == 0 || ZERO_0.equals(stopCode)) {
			stopCode = gStop.getStopId();
		}
		stopCode = STARTS_WITH_NRT_A00_.matcher(stopCode).replaceAll(StringUtils.EMPTY);
		if (stopCode.isEmpty()) {
			System.out.printf("\nUnexpected empty stop ID %s!\n", gStop);
			System.exit(-1);
			return -1;
		}
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
		} else if (stopCode.equals("MCC")) {
			return 100_045;
		}
		if (stopCode.equals("NiagSqua")) {
			return 200_000;
		} else if (stopCode.equals("Concentrix")) {
			return 200_001;
		} else if (stopCode.equals("FortErie")) {
			return 200_002;
		}
		System.out.printf("\nUnexpected stop ID %s!\n", gStop);
		System.exit(-1);
		return -1;
	}
}
