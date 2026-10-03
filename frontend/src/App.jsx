import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, Route, Routes, useLocation, useNavigate, useParams } from "react-router-dom";
import { QRCodeSVG } from "qrcode.react";
import { api, clearSession, getUser, setSession } from "./api";

function formatDate(iso) {
  if (!iso) return "";
  const d = new Date(`${iso}T00:00:00`);
  return d.toLocaleDateString("en-GB", { weekday: "short", day: "2-digit", month: "short", year: "numeric" });
}

function formatTime(t) {
  if (!t) return "";
  const [h, m] = String(t).split(":");
  const date = new Date();
  date.setHours(Number(h), Number(m || 0));
  return date.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });
}

function weekday(iso) {
  return new Date(`${iso}T00:00:00`).toLocaleDateString("en-GB", { weekday: "short" }).toUpperCase();
}

function dayNum(iso) {
  return new Date(`${iso}T00:00:00`).getDate();
}

function formatHoldTime(milliseconds) {
  const totalSeconds = Math.max(0, Math.ceil(milliseconds / 1000));
  const minutes = Math.floor(totalSeconds / 60).toString().padStart(2, "0");
  const seconds = (totalSeconds % 60).toString().padStart(2, "0");
  return `${minutes}:${seconds}`;
}

function fallbackArt(title, wide = false, genres = "") {
  const safeTitle = String(title || "CinePass").replace(/[<>&'\"]/g, "");
  const themes = {
    Interstellar: ["#071b35", "#2c6e9e", "#9ed8ff", "DEEP SPACE"],
    "Dune: Part Two": ["#3c1f18", "#b6532f", "#ffd28b", "DESERT EPIC"],
    "Deadpool & Wolverine": ["#300d16", "#b51d35", "#ffd166", "ACTION DUO"],
    "Joker: Folie à Deux": ["#171329", "#713b83", "#f2a7d5", "DARK DRAMA"],
    "Gladiator II": ["#201b18", "#8b5e34", "#f7d27d", "THE ARENA"],
    Wicked: ["#08251f", "#16806b", "#b7f7dc", "FANTASY MUSICAL"]
  };
  const [from, to, highlight, label] = themes[safeTitle] || ["#241b3d", "#a33359", "#ffd166", "NOW PLAYING"];
  const width = wide ? 1200 : 600;
  const height = wide ? 560 : 900;
  const fontSize = wide ? 72 : 42;
  const titleMarkup = wide ? "" : `<text x="8%" y="82%" fill="white" font-family="Arial,sans-serif" font-size="${fontSize}" font-weight="700">${safeTitle}</text>`;
  const genreMarkup = wide ? `<text x="8%" y="86%" fill="${highlight}" font-family="Arial,sans-serif" font-size="20" letter-spacing="5">${label}</text>` : `<text x="8%" y="89%" fill="${highlight}" font-family="Arial,sans-serif" font-size="16" letter-spacing="4">${label}</text>`;
  const scene = safeTitle === "Interstellar"
    ? `<circle cx="${width * .72}" cy="${height * .34}" r="${wide ? 120 : 76}" fill="#050812" stroke="${highlight}" stroke-width="${wide ? 12 : 8}"/><circle cx="${width * .72}" cy="${height * .34}" r="${wide ? 36 : 22}" fill="${highlight}" opacity=".8"/>`
    : safeTitle === "Dune: Part Two"
      ? `<path d="M0 ${height * .66} Q${width * .25} ${height * .48} ${width * .5} ${height * .67} T${width} ${height * .6} V${height} H0Z" fill="${highlight}" opacity=".28"/><circle cx="${width * .78}" cy="${height * .2}" r="${wide ? 86 : 54}" fill="${highlight}" opacity=".72"/>`
      : `<circle cx="${width * .78}" cy="${height * .2}" r="${wide ? 150 : 90}" fill="${highlight}" opacity=".32"/>`;
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="${from}"/><stop offset=".58" stop-color="${to}"/><stop offset="1" stop-color="#090c18"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/>${scene}<path d="M0 ${height * .8} Q${width * .35} ${height * .55} ${width} ${height * .76} V${height} H0Z" fill="#090c18" opacity=".78"/>${titleMarkup}${genreMarkup}</svg>`;
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

function MovieImage({ movie, kind = "poster", className = "", alt = "" }) {
  const source = kind === "backdrop" ? movie.backdropUrl : movie.posterUrl;
  const localArt = fallbackArt(movie.title, kind === "backdrop", movie.genres);
  return (
    <img
      className={className}
      src={localArt}
      alt={alt || movie.title}
      onError={(event) => {
        event.currentTarget.onerror = null;
        event.currentTarget.src = source || localArt;
      }}
    />
  );
}

function MovieBackdrop({ movie, className = "" }) {
  const localArt = fallbackArt(movie.title, true, movie.genres);
  const [source, setSource] = useState(localArt);
  return (
    <div
      className={className}
      role="img"
      aria-label={movie.title}
    >
      <img
        className="backdrop-image"
        src={source}
        alt=""
        onError={() => setSource(movie.backdropUrl || localArt)}
      />
    </div>
  );
}

function Nav({ onAuth }) {
  const loc = useLocation();
  const user = getUser();
  return (
    <header className="nav">
      <Link className="brand" to="/"><span className="mark">▶</span> CinePass</Link>
      <nav className="links">
        <Link className={loc.pathname === "/" ? "active" : ""} to="/">Home</Link>
        <Link className={loc.pathname === "/movies" || loc.pathname.startsWith("/movies/") ? "active" : ""} to="/movies">Movies</Link>
        <Link className={loc.pathname.startsWith("/bookings") ? "active" : ""} to="/bookings">My Bookings</Link>
      </nav>
      <div className="nav-right">
        <span className="city">📍 Vijayawada</span>
        {user ? <span>{user.fullName}</span> : <button className="ghost" onClick={onAuth}>Login</button>}
        {user && <button className="icon-btn" onClick={() => { clearSession(); window.location.href = "/"; }}>⎋</button>}
      </div>
    </header>
  );
}

function Steps({ current }) {
  const items = ["01 MOVIE", "02 SHOW", "03 SEATS", "04 PAYMENT", "05 COMPLETE"];
  return (
    <div className="steps">
      {items.map((item, i) => (
        <span key={item} className={i === current ? "on" : ""}>{item}</span>
      ))}
    </div>
  );
}

function Home() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const featured = movies.find((m) => m.status === "NOW_SHOWING") || movies[0];
  const showing = movies.filter((m) => m.status === "NOW_SHOWING");
  const coming = movies.filter((m) => m.status === "COMING_SOON");
  const loadMovies = () => {
    setLoading(true);
    setError("");
    api("/api/movies")
      .then(setMovies)
      .catch((err) => setError(err.message || "Unable to load movies."))
      .finally(() => setLoading(false));
  };
  useEffect(() => { loadMovies(); }, []);
  if (loading) return <div className="page">Loading showtimes...</div>;
  if (error) return (
    <div className="page">
      <div className="panel pad">
        <h2>We could not load showtimes</h2>
        <p className="muted">{error}. Make sure the backend services and API Gateway are running, then try again.</p>
        <button className="primary" onClick={loadMovies}>Retry</button>
      </div>
    </div>
  );
  if (!featured) return <div className="page">No showtimes are available yet.</div>;
  return (
    <div className="page">
      <section className="hero">
        <div className="hero-copy">
          <div className="kicker">{featured.genres}</div>
          <h1>{featured.title.toUpperCase()}</h1>
          <p className="meta">★ {featured.rating} ({featured.ratingCount}) &nbsp; {featured.duration} &nbsp; {featured.language}</p>
          <p className="muted">{featured.synopsis}</p>
          <div className="actions">
            <Link className="primary" style={{ display: "inline-block" }} to={`/movies/${featured.id}`}>Book Tickets</Link>
            <a className="ghost" href={featured.trailerUrl} target="_blank" rel="noreferrer">Watch Trailer</a>
          </div>
        </div>
        <MovieBackdrop movie={featured} className="hero-art" />
      </section>
      <h3 className="section-title">Now Showing</h3>
      <div className="row-scroll">
        {showing.map((movie) => (
          <Link key={movie.id} className="movie-card" to={`/movies/${movie.id}`}>
            <MovieImage movie={movie} className="poster" />
            <div className="body">
              <strong>{movie.title}</strong>
              <div className="stars">★ {movie.rating} · {movie.language}</div>
              <div className="muted" style={{ marginTop: 8 }}>Book Now →</div>
            </div>
          </Link>
        ))}
      </div>
      <h3 className="section-title">Coming Soon</h3>
      <div className="row-scroll coming">
        {coming.map((movie) => (
          <div key={movie.id} className="coming-card">
            <MovieImage movie={movie} kind="backdrop" />
            <span>{movie.title}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function Movies() {
  const [movies, setMovies] = useState([]);
  const [filter, setFilter] = useState("ALL");
  useEffect(() => { api("/api/movies").then(setMovies).catch(() => setMovies([])); }, []);
  const visible = filter === "ALL" ? movies : movies.filter((movie) => movie.status === filter);
  return (
    <div className="page browse-page">
      <div className="browse-heading">
        <div>
          <div className="kicker">The CinePass collection</div>
          <h1 className="display">FIND YOUR NEXT STORY</h1>
          <p className="muted">Browse what is playing now and plan your next cinema night.</p>
        </div>
        <div className="filter-tabs">
          {[['ALL', 'All movies'], ['NOW_SHOWING', 'Now showing'], ['COMING_SOON', 'Coming soon']].map(([value, label]) => (
            <button key={value} className={filter === value ? "primary" : "ghost"} onClick={() => setFilter(value)}>{label}</button>
          ))}
        </div>
      </div>
      <div className="movie-grid">
        {visible.map((movie) => (
          <Link key={movie.id} className="movie-card" to={`/movies/${movie.id}`}>
            <MovieImage movie={movie} className="poster" />
            <div className="body">
              <div className="movie-status">{movie.status === "NOW_SHOWING" ? "NOW SHOWING" : "COMING SOON"}</div>
              <strong>{movie.title}</strong>
              <div className="stars">★ {movie.rating || "New"} · {movie.language}</div>
              <div className="muted" style={{ marginTop: 8 }}>{movie.status === "NOW_SHOWING" ? "View showtimes →" : "View details →"}</div>
            </div>
          </Link>
        ))}
      </div>
      {!visible.length && <div className="panel pad">No movies are available right now.</div>}
    </div>
  );
}

function MovieDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [date, setDate] = useState("");
  const [selectedShowId, setSelectedShowId] = useState(null);
  useEffect(() => {
    api(`/api/movies/${id}`).then(setMovie);
    api(`/api/shows?movieId=${id}`).then((data) => {
      setShows(data);
      if (data.length) {
        const preferred = data.find((s) => s.showDate === "2026-09-15" && String(s.showTime).startsWith("18:00")) || data[0];
        setDate(preferred.showDate);
        setSelectedShowId(preferred.id);
      }
    });
  }, [id]);
  const dates = [...new Set(shows.map((s) => s.showDate))];
  const times = shows.filter((s) => s.showDate === date);
  const selectedShow = times.find((s) => s.id === selectedShowId) || times[0];
  const cinemas = [...new Set(times.map((s) => s.cinema))];
  if (!movie) return <div className="page">Loading...</div>;
  return (
    <div className="page">
      <Steps current={1} />
      <div className="details">
        <div>
          <MovieImage movie={movie} className="poster-lg" />
          <a className="ghost" style={{ display: "block", textAlign: "center", marginTop: 12 }} href={movie.trailerUrl} target="_blank" rel="noreferrer">Trailer</a>
        </div>
        <div>
          <h1 style={{ marginTop: 0 }}>{movie.title}</h1>
          <div className="stars">★ {movie.rating} ({movie.ratingCount})</div>
          <div className="chip-row">
            {movie.genres.split("·").map((g) => <span className="chip" key={g}>{g.trim()}</span>)}
            <span className="chip">{movie.duration}</span>
            <span className="chip">{movie.certificate}</span>
          </div>
          <p className="muted">{movie.synopsis}</p>
          <p><b>Director</b> &nbsp; {movie.director}</p>
          <p><b>Cast</b> &nbsp; {movie.castNames}</p>
          <h4>Select Date</h4>
          <div className="dates">
            {dates.map((d) => (
              <button key={d} className={`date ${d === date ? "active" : ""}`} onClick={() => {
                setDate(d);
                const firstShow = shows.find((show) => show.showDate === d);
                if (firstShow) setSelectedShowId(firstShow.id);
              }}>
                <small>{weekday(d)}</small>{dayNum(d)}
              </button>
            ))}
          </div>
          <div className="cinema-heading">
            <h4>Choose a cinema and showtime</h4>
            <span className="muted">{cinemas.length} cinemas · {times.length} shows</span>
          </div>
          <div className="cinema-list">
            {cinemas.map((cinema) => {
              const cinemaShows = times.filter((show) => show.cinema === cinema);
              return (
                <section className="cinema-card" key={cinema}>
                  <div className="cinema-info">
                    <strong>{cinema}</strong>
                    <span className="muted">{cinemaShows[0]?.screen} · English · ₹{cinemaShows[0]?.price}</span>
                  </div>
                  <div className="showtime-list">
                    {cinemaShows.map((show) => (
                      <button key={show.id} className={`time ${show.id === selectedShow?.id ? "active" : ""}`} onClick={() => setSelectedShowId(show.id)}>
                        {formatTime(show.showTime)}
                      </button>
                    ))}
                  </div>
                </section>
              );
            })}
          </div>
          <div className="actions">
            <button className="primary" disabled={!selectedShow} onClick={() => navigate(`/seats/${selectedShow.id}`, { state: { movie } })}>Book Tickets</button>
          </div>
        </div>
      </div>
    </div>
  );
}

function Seats() {
  const { showId } = useParams();
  const navigate = useNavigate();
  const [map, setMap] = useState(null);
  const [selected, setSelected] = useState(["A10", "A11"]);
  const [conflict, setConflict] = useState(null);
  const movie = useLocation().state?.movie;
  useEffect(() => {
    api(`/api/bookings/seats/${showId}`).then((data) => {
      setMap(data);
      setSelected((curr) => curr.filter((id) => {
        const all = data.grid.flatMap((r) => r.seats);
        const seat = all.find((s) => s.id === id);
        return seat && seat.status === "AVAILABLE";
      }));
    });
  }, [showId]);
  const price = map?.price || 200;
  const toggle = (seat) => {
    if (seat.status !== "AVAILABLE") return;
    setSelected((curr) => curr.includes(seat.id) ? curr.filter((id) => id !== seat.id) : [...curr, seat.id]);
  };
  const reserve = async () => {
    if (!getUser()) {
      navigate("/?auth=1");
      return;
    }
    try {
      const booking = await api("/api/bookings/reserve", {
        method: "POST",
        body: JSON.stringify({ showId: Number(showId), seats: selected })
      });
      navigate(`/pay/${booking.id}`, { state: { booking, movie } });
    } catch (err) {
      if (err.status === 409) {
        setConflict(err.payload?.seat || selected[0]);
        const data = await api(`/api/bookings/seats/${showId}`);
        setMap(data);
      } else if (err.status === 401) {
        navigate("/?auth=1");
      } else {
        alert(err.message);
      }
    }
  };
  if (!map) return <div className="page">Loading seats...</div>;
  const show = map.show;
  return (
    <div className="page">
      <Steps current={2} />
      <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 16 }}>
        {movie && <MovieImage movie={movie} className="mini-poster" alt="" />}
        <div>
          <strong>{movie?.title || show.movieTitle || "Interstellar"}</strong>
          <div className="muted">{formatDate(show.showDate)} | {formatTime(show.showTime)} | {show.cinema} · {show.screen}</div>
        </div>
      </div>
      <div className="seat-layout">
        <div className="panel pad">
          <div className="screen">SCREEN</div>
          {map.grid.map((row) => (
            <div className="seat-row" key={row.row}>
              <span className="row-id">{row.row}</span>
              {row.seats.map((seat) => (
                <button
                  key={seat.id}
                  className={`seat ${seat.status === "BOOKED" || seat.status === "HELD" ? "booked" : ""} ${selected.includes(seat.id) ? "selected" : ""}`}
                  onClick={() => toggle(seat)}
                />
              ))}
              <span className="row-id">{row.row}</span>
            </div>
          ))}
          <div className="legend">
            <span><i className="dot" style={{ background: "#22c55e" }} /> Selected</span>
            <span><i className="dot" style={{ background: "#2a3448" }} /> Available</span>
            <span><i className="dot" style={{ background: "#e11d48" }} /> Taken</span>
          </div>
        </div>
        <aside className="panel pad summary">
          <h3>Selected Seats</h3>
          <p>{selected.join(", ") || "None"}</p>
          <ul>
            <li>Tickets <b>{selected.length}</b></li>
            <li>Price <b>₹{price} × {selected.length}</b></li>
            <li>Total <b>₹{price * selected.length}</b></li>
          </ul>
          <button className="primary" style={{ width: "100%" }} disabled={!selected.length} onClick={reserve}>Proceed to Payment</button>
        </aside>
      </div>
      {conflict && (
        <div className="overlay">
          <div className="modal">
            <div className="warn-icon">!</div>
            <h3>Seat No Longer Available</h3>
            <p className="muted">This seat was just booked by another customer.</p>
            <p>Seat: <b>{conflict}</b></p>
            <p className="muted">Please select another available seat.</p>
            <button className="primary" onClick={() => { setConflict(null); setSelected([]); }}>Choose Another Seat</button>
          </div>
        </div>
      )}
    </div>
  );
}

function Payment() {
  const { id } = useParams();
  const navigate = useNavigate();
  const loc = useLocation();
  const [booking, setBooking] = useState(loc.state?.booking || null);
  const [method, setMethod] = useState("UPI");
  const [timeLeft, setTimeLeft] = useState(null);
  const [expired, setExpired] = useState(false);
  const movie = loc.state?.movie;
  useEffect(() => {
    if (!booking) {
      api(`/api/bookings/${id}`).then(setBooking).catch(() => navigate("/"));
    }
  }, [booking, id, navigate]);
  useEffect(() => {
    if (!booking?.expiresAt) return undefined;
    const update = () => {
      const remaining = new Date(booking.expiresAt).getTime() - Date.now();
      setTimeLeft(Math.max(0, remaining));
      setExpired(remaining <= 0);
    };
    update();
    const timer = window.setInterval(update, 1000);
    return () => window.clearInterval(timer);
  }, [booking?.expiresAt]);
  if (!booking) return <div className="page">Loading payment...</div>;
  const pay = async () => {
    if (expired || timeLeft === 0) {
      setExpired(true);
      return;
    }
    const confirmed = await api(`/api/bookings/${id}/pay`, { method: "POST", body: JSON.stringify({ method }) });
    navigate(`/ticket/${confirmed.id}`, { state: { booking: confirmed, movie } });
  };
  return (
    <div className="page">
      <Steps current={3} />
      <div className={`hold-banner ${expired ? "expired" : ""}`}>
        <span className="hold-icon">⏱</span>
        <div>
          <strong>{expired ? "Your seat hold has expired" : "Seats held for you"}</strong>
          <div className="muted">{expired ? "Choose your seats again to continue." : "Complete payment before the timer ends."}</div>
        </div>
        {!expired && timeLeft !== null && <strong className="hold-timer">{formatHoldTime(timeLeft)}</strong>}
      </div>
      <div className="pay-grid">
        <div className="panel pad summary">
          <h3>Booking Summary</h3>
          <p><b>{booking.movieTitle}</b></p>
          <p className="muted">{booking.cinema}<br />{booking.screen}<br />{booking.showDate} | {booking.showTime}<br />Seats: {booking.seats}</p>
          <ul>
            <li>Tickets ({booking.ticketCount}) <b>₹{booking.ticketPrice}</b></li>
            <li>Convenience Fee <b>₹{booking.convenienceFee}</b></li>
          </ul>
          <hr />
          <p>Total <b>₹{booking.total}</b></p>
          <h4>Payment Method</h4>
          {["UPI", "CARD", "NETBANKING"].map((item) => (
            <div key={item} className={`pay-option ${method === item ? "active" : ""}`} onClick={() => setMethod(item)}>
              <span>{item === "CARD" ? "Credit / Debit Card" : item === "NETBANKING" ? "Net Banking" : "UPI"}</span>
              <input type="radio" checked={method === item} readOnly />
            </div>
          ))}
          <button className="primary" style={{ width: "100%", marginTop: 12 }} disabled={expired} onClick={pay}>{expired ? "Hold expired" : `Pay ₹${booking.total}`}</button>
          <p className="muted" style={{ fontSize: 12 }}>Secure payments · Powered by Razorpay</p>
        </div>
        <MovieBackdrop movie={movie || { title: booking.movieTitle }} className="pay-art" />
      </div>
    </div>
  );
}

function Ticket() {
  const { id } = useParams();
  const [booking, setBooking] = useState(useLocation().state?.booking || null);
  useEffect(() => {
    if (!booking) api(`/api/bookings/${id}`).then(setBooking);
  }, [booking, id]);
  if (!booking) return <div className="page">Loading ticket...</div>;
  return (
    <div className="page ticket-wrap">
      <div className="ticket">
        <div className="muted">CinePass</div>
        <h2>BOOKING CONFIRMED</h2>
        <dl>
          <dt>Movie</dt><dd>{booking.movieTitle}</dd>
          <dt>Cinema</dt><dd>{booking.cinema}</dd>
          <dt>Screen</dt><dd>{booking.screen}</dd>
          <dt>Date</dt><dd>{booking.showDate}</dd>
          <dt>Time</dt><dd>{booking.showTime}</dd>
          <dt>Seats</dt><dd>{booking.seats}</dd>
          <dt>Tickets</dt><dd>{booking.ticketCount}</dd>
          <dt>Total</dt><dd>₹{booking.total}</dd>
          <dt>Booking ID</dt><dd>{booking.bookingCode}</dd>
        </dl>
        <div className="qr"><QRCodeSVG value={booking.bookingCode} size={140} /></div>
        <p style={{ letterSpacing: ".12em", fontWeight: 800 }}>SCAN FOR ENTRY</p>
        <p className="muted">Valid for staff verification at the cinema entrance</p>
        <Link to="/bookings">Go to My Bookings</Link>
      </div>
    </div>
  );
}

function Bookings() {
  const [items, setItems] = useState([]);
  const [tab, setTab] = useState("upcoming");
  const [movies, setMovies] = useState([]);
  useEffect(() => {
    api("/api/bookings/mine").then(setItems).catch(() => setItems([]));
    api("/api/movies").then(setMovies);
  }, []);
  const posters = useMemo(() => Object.fromEntries(movies.map((m) => [m.id, m.posterUrl])), [movies]);
  const upcoming = items.filter((b) => b.status === "CONFIRMED" || b.status === "HELD");
  const previous = items.filter((b) => b.status !== "CONFIRMED" && b.status !== "HELD");
  const list = tab === "upcoming" ? upcoming : previous;
  if (!getUser()) return <Navigate to="/?auth=1" />;
  return (
    <div className="page">
      <h2>My Bookings</h2>
      <div className="actions">
        <button className={tab === "upcoming" ? "primary" : "ghost"} onClick={() => setTab("upcoming")}>Upcoming</button>
        <button className={tab === "previous" ? "primary" : "ghost"} onClick={() => setTab("previous")}>Previous</button>
      </div>
      {list.map((b) => (
        <div className="panel booking-item" key={b.id}>
          <MovieImage movie={{ title: b.movieTitle, posterUrl: posters[b.movieId] }} alt="" />
          <div>
            <strong>{b.movieTitle}</strong>
            <div className="muted">{b.showDate} · {b.showTime}<br />{b.cinema} · {b.screen}<br />Seats: {b.seats}<br />Booking ID: {b.bookingCode}</div>
          </div>
          <div>
            <span className={`badge ${b.status === "CONFIRMED" ? "up" : "done"}`}>{b.status === "CONFIRMED" ? "Upcoming" : b.status}</span>
            <div style={{ marginTop: 8 }}>
              {b.status === "HELD" && <Link to={`/pay/${b.id}`}>Pay Now</Link>}
              {b.status === "CONFIRMED" && <Link to={`/ticket/${b.id}`}>View Ticket</Link>}
            </div>
          </div>
        </div>
      ))}
      {!list.length && <p className="muted">No bookings yet.</p>}
    </div>
  );
}

function CheckIn() {
  const [code, setCode] = useState("");
  const [result, setResult] = useState(null);
  const verify = async () => {
    try {
      setResult(await api(`/api/bookings/verify/${code.trim()}`));
    } catch {
      setResult({ valid: false });
    }
  };
  return (
    <div className="page">
      <div className="panel checkin">
        <h2>CinePass Check-In</h2>
        <p className="muted">Scan the QR code from the ticket</p>
        <input type="text" placeholder="Enter booking ID" value={code} onChange={(e) => setCode(e.target.value)} />
        <button className="primary" style={{ marginTop: 12 }} onClick={verify}>Verify</button>
        {result && result.valid && (
          <div className="valid">
            <h3>TICKET VERIFIED</h3>
            <p>{result.booking.movieTitle}<br />Screen {result.booking.screen}<br />{result.booking.showTime}<br />Seats: {result.booking.seats}</p>
            <strong>VALID TICKET</strong>
          </div>
        )}
        {result && !result.valid && <p className="muted">Ticket not valid.</p>}
      </div>
    </div>
  );
}

function AuthModal({ onClose }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const path = mode === "login" ? "/api/users/login" : "/api/users/register";
      const data = await api(path, { method: "POST", body: JSON.stringify(form) });
      setSession(data.token, data.user);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };
  const field = (key) => (e) => setForm({ ...form, [key]: e.target.value });
  return (
    <div className="overlay">
      <div className="auth-shell">
        <div className="auth-promo">
          <button className="auth-close" type="button" onClick={onClose} aria-label="Close sign in">×</button>
          <div className="auth-play">▶</div>
          <div className="kicker">Your next night out</div>
          <h2>Movies feel better together.</h2>
          <p>Save your favorite shows, choose seats faster, and keep every ticket in one place.</p>
          <div className="auth-perks"><span>✓ Fast booking</span><span>✓ Secure tickets</span><span>✓ Easy check-in</span></div>
        </div>
        <form className="auth-card auth-form" onSubmit={submit}>
          <div className="auth-tabs">
            <button type="button" className={mode === "login" ? "active" : ""} onClick={() => { setMode("login"); setError(""); }}>Log in</button>
            <button type="button" className={mode === "register" ? "active" : ""} onClick={() => { setMode("register"); setError(""); }}>Create account</button>
          </div>
          <h2>{mode === "login" ? "Welcome back" : "Create your account"}</h2>
          <p className="muted">{mode === "login" ? "Continue your cinema journey." : "It takes less than a minute."}</p>
          {mode === "register" && <input className="auth-input" placeholder="Full name" value={form.fullName} onChange={field("fullName")} required />}
          <input className="auth-input" type="email" placeholder="Email address" value={form.email} onChange={field("email")} required />
          <div className="password-field">
            <input className="auth-input" type={showPassword ? "text" : "password"} placeholder="Password" value={form.password} onChange={field("password")} minLength={6} required />
            <button type="button" onClick={() => setShowPassword(!showPassword)}>{showPassword ? "Hide" : "Show"}</button>
          </div>
          {error && <p className="auth-error">{error}</p>}
          <button className="primary auth-submit" disabled={busy}>{busy ? "Please wait..." : mode === "login" ? "Log in" : "Create account"}</button>
          <p className="auth-switch">{mode === "login" ? "New to CinePass?" : "Already have an account?"} <button type="button" onClick={() => { setMode(mode === "login" ? "register" : "login"); setError(""); }}>{mode === "login" ? "Create one" : "Log in"}</button></p>
        </form>
      </div>
    </div>
  );
}

export default function App() {
  const [auth, setAuth] = useState(false);
  const loc = useLocation();
  useEffect(() => {
    if (new URLSearchParams(loc.search).get("auth") === "1") setAuth(true);
  }, [loc.search]);
  return (
    <div>
      <Nav onAuth={() => setAuth(true)} />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/movies" element={<Movies />} />
        <Route path="/movies/:id" element={<MovieDetails />} />
        <Route path="/seats/:showId" element={<Seats />} />
        <Route path="/pay/:id" element={<Payment />} />
        <Route path="/ticket/:id" element={<Ticket />} />
        <Route path="/bookings" element={<Bookings />} />
        <Route path="/check-in" element={<CheckIn />} />
      </Routes>
      {auth && <AuthModal onClose={() => setAuth(false)} />}
    </div>
  );
}
